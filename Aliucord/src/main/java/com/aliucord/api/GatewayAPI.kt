package com.aliucord.api

import com.aliucord.Logger
import com.aliucord.api.GatewayAPI.EventListener
import com.aliucord.patcher.*
import com.discord.gateway.GatewaySocket
import com.discord.gateway.`GatewaySocket$connect$$inlined$apply$lambda$4`
import com.discord.models.deserialization.gson.InboundGatewayGsonParser
import com.discord.stores.StoreGatewayConnection
import com.discord.stores.StoreStream
import com.discord.utilities.websocket.RawMessageHandler
import com.discord.utilities.websocket.WebSocket
import com.google.gson.stream.JsonReader
import org.json.JSONObject
import java.io.StringReader

/**
 * Api for easily listening to gateway events
 */
object GatewayAPI {

    fun interface EventListener<T> {

        operator fun invoke(eventData: T)

    }

    val eventListeners = mutableListOf<Triple<String, Class<*>, EventListener<Any>>>()
    private val rawListeners = mutableListOf<EventListener<String>>()
    private val socket = StoreGatewayConnection::class.java.getDeclaredField("socket").apply { isAccessible = true }
    private val logger = Logger("GatewayAPI")
    private val patcher = PatcherAPI(logger)

    init {
        addRawMessageHandler()
        patchRawMessageHandler()
    }

    private fun addRawMessageHandler() {
        patcher.before<WebSocket>("setRawMessageHandler", RawMessageHandler::class.java) { (param, rawMessageHandler: RawMessageHandler?) ->
            if (rawMessageHandler == null) param.args[0] = `GatewaySocket$connect$$inlined$apply$lambda$4`(
                socket[StoreStream.getGatewaySocket()] as GatewaySocket, null
            )
        }
    }

    private fun patchRawMessageHandler() {
        patcher.after<`GatewaySocket$connect$$inlined$apply$lambda$4`>("onRawMessage", String::class.java) { (_, rawEvent: String) ->
            val event = JSONObject(rawEvent)
            val eventName = event.getString("t")
            val eventData = event["d"].toString()

            eventListeners.filter { (name, _) -> name == eventName }.forEach { (_, type, listener) ->
                try {
                    val data = InboundGatewayGsonParser.fromJson(JsonReader(StringReader(eventData)), type)
                    listener.invoke(data)
                } catch (e: Throwable) {
                    logger.error("Failed to serialize data for event: $eventName", e)
                }
            }

            rawListeners.forEach { listener ->
                listener.invoke(rawEvent)
            }
        }
    }

    /**
     * Listens to all raw gateway events
     *
     * @param listener The method that gets called when a gateway event is received, it is passed the full event string rather than just the data.
     */
    @JvmStatic
    fun onRawEvent(listener: (rawEvent: String) -> Unit) {
        rawListeners.add(EventListener { eventData ->
            listener(eventData)
        })
    }

    /**
     * Listens to a specific gateway event
     *
     * @param names List of event names to listen to (Case insensitive).
     * @param listener The method that gets called when any of the gateway events are received, it is passed the full event string rather than just the data.
     */
    @JvmStatic
    fun onRawEvent(names: List<String>, listener: (rawEvent: String) -> Unit) {
        rawListeners.add(EventListener { eventData ->
            val namesUpper = names.map { it.uppercase() }
            val eventName = JSONObject(eventData).getString("t")
            if (namesUpper.contains(eventName)) listener(eventData)
        })
    }

    /**
     * Listens to a specific gateway event
     *
     * @param name The name of the event (Case insensitive).
     * @param listener The method that gets called when the gateway event is received, it is passed the full event string rather than just the data.
     */
    @JvmStatic
    fun onRawEvent(name: String, listener: (rawEvent: String) -> Unit) = onRawEvent(listOf(name), listener)

    /**
     * Listens to a specific gateway event
     *
     * @param name The name of the event (Case insensitive).
     * @param listener The method that gets called when the gateway event is received, it is passed a deserialized model of type `T`.
     */
    inline fun <reified T : Any> onEvent(name: String, crossinline listener: (T) -> Unit) {
        val eventListener = EventListener<Any> { eventData -> listener(eventData as T) }
        eventListeners.add(Triple(name.uppercase(), T::class.java, eventListener))
    }

    /**
     * Listens to a specific gateway event
     *
     * @param name The name of the event (Case insensitive).
     * @param clazz The type that the events data should be deserialized to.
     * @param listener The method that gets called when the gateway event is received, it is passed an instance of `clazz`.
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> onEvent(name: String, clazz: Class<T>, listener: (T) -> Unit) {
        val eventListener = EventListener<Any> { eventData -> listener(eventData as T) }
        eventListeners.add(Triple(name.uppercase(), clazz, eventListener))
    }

}
