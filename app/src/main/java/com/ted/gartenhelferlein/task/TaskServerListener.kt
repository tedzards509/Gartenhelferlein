package com.ted.gartenhelferlein.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

@Serializable
data class ReceivedMessage(
    val type: String,
    val path: String,
    val data: List<Task>? = null,
    val task: Task? = null
)

@Serializable
data class SendCreateMessage(
    val type: String,
    val path: String,
    val data: Task
)

@Serializable
data class SendReadMessage(
    val type: String,
    val path: String,
)

@Serializable
data class SendUpdateMessage(
    val type: String,
    val path: String,
    val id: Int,
    val data: Task
)

@Serializable
data class SendDeleteMessage(
    val type: String,
    val path: String,
    val id: Int,
)

class MessageClient(private val url: String) : WebSocketListener(), CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var webSocket: WebSocket? = null
    private var messageListener: MessageListener? = null

    interface MessageListener {
        fun onMessage(message: String)
        fun onConnect()
        fun onFailToConnect()
    }

    fun setMessageListener(listener: MessageListener) {
        messageListener = listener
    }

    fun removeMessageListener() {
        messageListener = null
    }

    fun connect() {
        println("Attempting to connect to $url")
        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient.Builder()
            .readTimeout(1000, TimeUnit.MILLISECONDS)
            .build()

        try {
            webSocket = client.newWebSocket(request, this)
        } catch (e: Exception) {
            messageListener?.onFailToConnect()
        }
    }

    fun send(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
        job.cancel()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        println("Connection established")
        messageListener?.onConnect()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        messageListener?.onMessage(text)
    }
}
