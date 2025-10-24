package com.aliucord.api

import com.aliucord.Logger
import com.aliucord.patcher.*
import com.aliucord.utils.GsonUtils.fromJson
import com.aliucord.utils.SerializedName
import com.aliucord.utils.lazyField
import com.discord.gateway.GatewaySocket
import com.discord.gateway.`GatewaySocket$connect$$inlined$apply$lambda$4`
import com.discord.models.deserialization.gson.InboundGatewayGsonParser
import com.discord.stores.StoreGatewayConnection
import com.discord.stores.StoreStream
import com.discord.utilities.websocket.RawMessageHandler
import com.discord.utilities.websocket.WebSocket
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.StringReader

/**
 * Api for easily listening to gateway events
 */
object GatewayAPI {

    fun interface EventListener<in T> {

        operator fun invoke(eventData: T)

    }

    /**
     * A listener registered with [onEvent]
     *
     * @param eventName Name of the event to listen to
     * @param type The type to serialize the event data into
     * @param listener Callback for the incoming event
     */
    @PublishedApi
    internal data class RegisteredEventListener<T: Any>(
        val eventName: String,
        val type: Class<*>,
        val listener: EventListener<T>
    )

    /**
     * Model used to extract the name from a gateway event, this is done before we know what type to deserialize data into
     * which is why [EventData] is separated
     *
     * @param name The name of the event
     */
    private data class EventName(
        @SerializedName("t") val name: String
    )

    /**
     * Model used to deserialize event data
     *
     * @see EventName for why this isn't a combined model
     *
     * @param T The type to deserialize data into, usually specified by a plugin
     * @param data The deserialized data
     */
    private data class EventData<T>(
        @SerializedName("d") val data: T
    )

    @PublishedApi
    internal val registeredEventListeners = mutableListOf<RegisteredEventListener<Any>>()
    private val rawListeners = mutableListOf<EventListener<String>>()
    private val socket by lazyField<StoreGatewayConnection>()
    private val logger = Logger("GatewayAPI")
    private val patcher = PatcherAPI(logger)

    init {
        addRawMessageHandler()
        patchRawMessageHandler()
    }

    /**
     * Coerces a string to match the casing of a gateway event name
     *
     * Ex. `Message create` -> `MESSAGE_CREATE`
     */
    @PublishedApi
    internal fun String.asEventName() = uppercase().replace(" ", "_")

    @Suppress("InconsistentCommentForJavaParameter")
    private fun getEventName(json: String): String {
        return InboundGatewayGsonParser.fromJson(
            /* jsonReader = */ JsonReader(StringReader(json)),
            /* aClass = */ EventName::class.java
        ).name
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
            if(registeredEventListeners.isNotEmpty()) {
                val eventName = getEventName(rawEvent)

                registeredEventListeners.filter { (name) -> name == eventName }.forEach { (_, type, listener) ->
                    try {
                        val data = InboundGatewayGsonParser.INSTANCE
                            .gatewayGsonInstance // The underlying Gson is needed in order to use a Type with generics since InboundGatewayGsonParser doesn't have a method for it
                            .fromJson<EventData<Any>>(
                                json = rawEvent,
                                type = TypeToken
                                    .getParameterized(EventData::class.java, type)
                                    .type
                            ).data

                        listener(data)
                    } catch (e: Throwable) {
                        logger.error("Failed to serialize data for event: $eventName", e)
                    }
                }
            }

            if(rawListeners.isNotEmpty()) {
                rawListeners.forEach { listener ->
                    listener(rawEvent)
                }
            }

        }
    }

    /**
     * Listens to all raw gateway events
     *
     * @param listener The method that gets called when a gateway event is received, it is passed the full event string rather than just the data.
     */
    @JvmStatic
    fun onRawEvent(listener: EventListener<String>) = rawListeners.add(listener)

    /**
     * Listens to a specific set of gateway events
     *
     * See [user docs](https://docs.discord.sex/topics/gateway-events#receive-events) for a list of possible events
     *
     * @param names List of event names to listen to (Case insensitive).
     * @param listener The method that gets called when any of the gateway events are received, it is passed the full event string rather than just the data.
     */
    @JvmStatic
    fun onRawEvent(names: List<String>, listener: (rawEvent: String) -> Unit) {
        rawListeners.add(EventListener { eventData ->
            val namesUpper = names.map { it.asEventName() }
            val eventName = getEventName(eventData)
            if (namesUpper.contains(eventName)) listener(eventData)
        })
    }

    /**
     * Listens to a specific gateway event
     *
     * See [user docs](https://docs.discord.sex/topics/gateway-events#receive-events) for a list of possible events
     *
     * @param name The name of the event (Case insensitive).
     * @param listener The method that gets called when the gateway event is received, it is passed the full event string rather than just the data.
     */
    @JvmStatic
    fun onRawEvent(name: String, listener: (rawEvent: String) -> Unit) {
        rawListeners.add(EventListener { eventData ->
            val nameUpper = name.asEventName()
            val eventName = getEventName(eventData)
            if (nameUpper == eventName) listener(eventData)
        })
    }

    /**
     * Listens to a specific gateway event
     *
     * See [user docs](https://docs.discord.sex/topics/gateway-events#receive-events) for a list of possible events
     *
     * @param name The name of the event (Case insensitive).
     * @param listener The method that gets called when the gateway event is received, it is passed a deserialized model of type [T].
     */
    inline fun <reified T : Any> onEvent(name: String, crossinline listener: (T) -> Unit) {
        val eventListener = EventListener<Any> { eventData -> listener(eventData as T) }
        registeredEventListeners.add(
            RegisteredEventListener(
                eventName = name.asEventName(),
                type = T::class.java,
                listener = eventListener
            )
        )
    }

    /**
     * Listens to a specific gateway event
     *
     * See [user docs](https://docs.discord.sex/topics/gateway-events#receive-events) for a list of possible events
     *
     * @param name The name of the event (Case insensitive).
     * @param clazz The type that the event's data should be deserialized to.
     * @param listener The method that gets called when the gateway event is received, it is passed an instance of [clazz].
     */
    // This overload is mostly just for Java plugins, since Java doesn't have reified type parameters
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> onEvent(name: String, clazz: Class<T>, listener: (T) -> Unit) {
        val eventListener = EventListener<Any> { eventData -> listener(eventData as T) }
        registeredEventListeners.add(
            RegisteredEventListener(
                eventName = name.asEventName(),
                type = clazz,
                listener = eventListener
            )
        )
    }

}
