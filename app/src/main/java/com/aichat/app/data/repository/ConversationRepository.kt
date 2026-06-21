package com.aichat.app.data.repository

import android.content.Context
import com.aichat.app.domain.model.ChatMessage
import com.aichat.app.domain.model.Conversation
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val historyDir: File
        get() = File(context.filesDir, "conversations").also { it.mkdirs() }

    suspend fun getAll(): List<Conversation> = withContext(Dispatchers.IO) {
        historyDir.listFiles()
            ?.filter { it.extension == "json" && it.name != "index.json" }
            ?.mapNotNull { file ->
                try {
                    gson.fromJson(file.readText(), Conversation::class.java)
                } catch (_: Exception) { null }
            }
            ?.sortedByDescending { it.updatedAt }
            ?: emptyList()
    }

    suspend fun get(id: String): Conversation? = withContext(Dispatchers.IO) {
        val file = File(historyDir, "$id.json")
        if (file.exists()) {
            try {
                gson.fromJson(file.readText(), Conversation::class.java)
            } catch (_: Exception) { null }
        } else null
    }

    suspend fun save(conversation: Conversation) = withContext(Dispatchers.IO) {
        val file = File(historyDir, "${conversation.id}.json")
        file.writeText(gson.toJson(conversation))
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        File(historyDir, "$id.json").delete()
    }

    /**
     * Generate a title from the first user message.
     */
    fun generateTitle(messages: List<ChatMessage>): String {
        val firstUserMsg = messages.firstOrNull { it.isUser }?.content?.trim() ?: return "\u65b0\u5bf9\u8bdd"
        val cleaned = firstUserMsg.take(30).replace("\n", " ")
        return if (firstUserMsg.length > 30) "$cleaned..." else cleaned
    }
}
