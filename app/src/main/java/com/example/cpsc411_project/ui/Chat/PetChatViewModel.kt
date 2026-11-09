package com.example.cpsc411_project.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpsc411_project.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class PetChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Holds the pet's temporary reaction speech bubble
    private val _petReaction = MutableStateFlow<String?>(null)
    val petReaction: StateFlow<String?> = _petReaction.asStateFlow()

    fun clearReaction() {
        _petReaction.value = null
    }

    private val config = generationConfig {
        maxOutputTokens = 100
        temperature = 0.8f
    }

    private fun getSystemInstruction(petType: String, happiness: Int, hunger: Int, clean: Int) = content {
        val mood = when {
            hunger < 30 -> "very hungry and begging for food"
            clean < 30 -> "dirty and uncomfortable"
            happiness < 30 -> "sad and lonely"
            happiness > 80 -> "ecstatic and playful"
            else -> "content and relaxed"
        }

        text("""
            You are a $petType virtual pet. You are NOT an AI or a language model.
            You are currently feeling $mood.
            Your stats: Happiness $happiness/100, Hunger $hunger/100, Cleanliness $clean/100.
            Always respond in character as a $petType. Use actions in asterisks like *meows* or *wags tail*.
            Keep responses very short (1-2 sentences). Never break character.
        """.trimIndent())
    }

    fun reactToAction(action: String, petType: String, happiness: Int, hunger: Int, clean: Int) {
        viewModelScope.launch {
            try {
                val model = GenerativeModel(
                    modelName = "gemini-3.1-flash-lite",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    generationConfig = config,
                    systemInstruction = getSystemInstruction(petType, happiness, hunger, clean),
                    requestOptions = RequestOptions(apiVersion = "v1")
                )

                val prompt = "Your owner just clicked the '$action' button. React to this action."
                val response = model.generateContent(prompt)

                // Update the state so the UI shows the speech bubble
                _petReaction.value = response.text

            } catch (e: Exception) {
                println("Gemini API Error: ${e.message}")
            }
        }
    }

    fun sendMessage(message: String, petType: String, happiness: Int, hunger: Int, clean: Int) {
        _messages.value += ChatMessage(message, isUser = true)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val model = GenerativeModel(
                    modelName = "gemini-3.1-flash-lite",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    generationConfig = config,
                    systemInstruction = getSystemInstruction(petType, happiness, hunger, clean),
                    requestOptions = RequestOptions(apiVersion = "v1")
                )

                val chat = model.startChat()
                val response = chat.sendMessage(message)

                _messages.value += ChatMessage(response.text ?: "...", isUser = false)

            } catch (e: Exception) {
                if (e.message?.contains("429") == true || e.message?.contains("quota", ignoreCase = true) == true) {
                    _messages.value += ChatMessage("(Your pet is sleeping... You've reached the API limit for now. Try again later!)", isUser = false)
                } else {
                    _messages.value += ChatMessage("Error: ${e.message}", isUser = false)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}