package com.example.cpsc411_project.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpsc411_project.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
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
        maxOutputTokens = 60
        temperature = 0.7f
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
                    modelName = "gemini-3.5-flash-lite",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    generationConfig = config,
                    systemInstruction = getSystemInstruction(petType, happiness, hunger, clean)
                )

                val prompt = "Your owner just clicked the '$action' button. React to this action."
                val response = model.generateContent(prompt)

                // Update the state so the UI shows the speech bubble
                _petReaction.value = response.text

            } catch (e: Exception) {
                println("Gemini API Error: ${e.message}")
                e.printStackTrace()
                // In-character fallback reaction if AI is temporarily unavailable
                _petReaction.value = when (action) {
                    "Feed" -> "*happily munches the food*"
                    "Play" -> "*bounces around playfully*"
                    "Pet" -> "*leans into your hand happily*"
                    else -> "*looks at you with affection*"
                }
            }
        }
    }

    fun sendMessage(message: String, petType: String, happiness: Int, hunger: Int, clean: Int) {
        _messages.value += ChatMessage(message, isUser = true)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val model = GenerativeModel(
                    modelName = "gemini-3.5-flash-lite",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    generationConfig = config,
                    systemInstruction = getSystemInstruction(petType, happiness, hunger, clean)
                )

                val chat = model.startChat()
                var fullResponse = ""
                var messageAdded = false

                try {
                    chat.sendMessageStream(message).collect { chunk ->
                        val textChunk = chunk.text.orEmpty()
                        if (textChunk.isNotEmpty()) {
                            fullResponse += textChunk
                            if (!messageAdded) {
                                _isLoading.value = false
                                _messages.value = _messages.value + ChatMessage(fullResponse, isUser = false)
                                messageAdded = true
                            } else {
                                _messages.value = _messages.value.dropLast(1) + ChatMessage(fullResponse, isUser = false)
                            }
                        }
                    }
                    if (!messageAdded) {
                        _messages.value = _messages.value + ChatMessage("...", isUser = false)
                    }
                } catch (e: Exception) {
                    val err = e.message.orEmpty()
                    if (!messageAdded && (err.contains("503") || err.contains("high demand", ignoreCase = true) || err.contains("UNAVAILABLE", ignoreCase = true))) {
                        kotlinx.coroutines.delay(1000)
                        chat.sendMessageStream(message).collect { chunk ->
                            val textChunk = chunk.text.orEmpty()
                            if (textChunk.isNotEmpty()) {
                                fullResponse += textChunk
                                if (!messageAdded) {
                                    _isLoading.value = false
                                    _messages.value = _messages.value + ChatMessage(fullResponse, isUser = false)
                                    messageAdded = true
                                } else {
                                    _messages.value = _messages.value.dropLast(1) + ChatMessage(fullResponse, isUser = false)
                                }
                            }
                        }
                    } else {
                        throw e
                    }
                }

            } catch (e: Exception) {
                println("Gemini Chat Error: ${e.message}")
                e.printStackTrace()
                val errorMsg = e.message.orEmpty() + " " + (e.cause?.message ?: "")
                val friendlyMessage = when {
                    errorMsg.contains("429") || errorMsg.contains("quota", ignoreCase = true) -> {
                        "(Your pet is sleeping... You've reached the API limit for now. Try again later!)"
                    }
                    errorMsg.contains("503") || errorMsg.contains("high demand", ignoreCase = true) || errorMsg.contains("UNAVAILABLE", ignoreCase = true) -> {
                        "(Your pet got distracted... The AI server is experiencing high demand. Please try again in a moment!)"
                    }
                    errorMsg.contains("API_KEY_INVALID", ignoreCase = true) || errorMsg.contains("API key not valid", ignoreCase = true) -> {
                        "(Invalid API key. Please check your GEMINI_API_KEY in local.properties.)"
                    }
                    else -> {
                        "(Your pet tilted their head in confusion and didn't catch that. Please try again!)"
                    }
                }
                _messages.value += ChatMessage(friendlyMessage, isUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}