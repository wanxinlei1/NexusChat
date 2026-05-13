package com.aichat.app.data.remote

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = false
)

@JsonAdapter(MessageAdapter::class)
data class Message(
    val role: String,
    val content: Any  // String for text-only, List<ContentPart> for multimodal
)

data class ContentPart(
    val type: String,        // "text" or "image_url"
    val text: String? = null,
    @SerializedName("image_url")
    val imageUrl: ImageUrl? = null
)

data class ImageUrl(val url: String)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ResponseMessage
)

data class ResponseMessage(
    val content: String,
    @SerializedName("reasoning_content")
    val reasoningContent: String? = null
)

/**
 * Custom Gson adapter that handles Message.content being either a plain String
 * or a List<ContentPart>.
 */
class MessageAdapter : JsonSerializer<Message>, JsonDeserializer<Message> {

    override fun serialize(src: Message, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj.addProperty("role", src.role)
        when (val c = src.content) {
            is String -> obj.addProperty("content", c)
            is List<*> -> {
                val arr = JsonArray()
                for (item in c) {
                    arr.add(context.serialize(item))
                }
                obj.add("content", arr)
            }
        }
        return obj
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Message {
        val obj = json.asJsonObject
        val role = obj.get("role").asString
        val contentElement = obj.get("content")

        val content: Any = if (contentElement.isJsonPrimitive) {
            contentElement.asString
        } else {
            val listType = object : TypeToken<List<ContentPart>>() {}.type
            context.deserialize<List<ContentPart>>(contentElement, listType)
        }

        return Message(role = role, content = content)
    }
}
