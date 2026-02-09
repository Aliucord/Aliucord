package com.discord.rtcconnection.socket.io

// @Suppress("unused")
object Opcodes {
    const val IDENTIFY = 0
    const val SELECT_PROTOCOL = 1
    const val READY = 2
    const val HEARTBEAT = 3
    const val SELECT_PROTOCOL_ACK = 4
    const val SPEAKING = 5
    const val HEARTBEAT_ACK = 6
    const val RESUME = 7
    const val HELLO = 8
    const val RESUMED = 9
    const val VIDEO = 12
    const val CLIENT_DISCONNECT = 13
    const val SESSION_UPDATE = 14
    const val MEDIA_SINK_WANTS = 15

    const val CLIENTS_CONNECT = 11
    const val CLIENT_FLAGS = 18
    const val CLIENT_PLATFORM = 20
    const val DAVE_PREPARE_TRANSITION = 21 // server
    const val DAVE_EXECUTE_TRANSITION = 22 // server
    const val DAVE_TRANSITION_READY = 23 // client
    const val DAVE_PREPARE_EPOCH = 24 // server
    const val DAVE_MLS_EXTERNAL_SENDER = 25 // server, binary
    const val DAVE_MLS_KEY_PACKAGE = 26 // client, binary
    const val DAVE_MLS_PROPOSALS = 27 // server, binary
    const val DAVE_MLS_COMMIT_WELCOME = 28 // client, binary
    const val DAVE_MLS_ANNOUNCE_COMMIT_TRANSITION = 29 // server, binary
    const val DAVE_MLS_WELCOME = 30 // server, binary
    const val DAVE_MLS_INVALID_COMMIT_WELCOME = 31 // client

    val nameLookup: Map<Int, String> = mapOf(
        0 to "IDENTIFY",
        1 to "SELECT_PROTOCOL",
        2 to "READY",
        3 to "HEARTBEAT",
        4 to "SELECT_PROTOCOL_ACK",
        5 to "SPEAKING",
        6 to "HEARTBEAT_ACK",
        7 to "RESUME",
        8 to "HELLO",
        9 to "RESUMED",
        12 to "VIDEO",
        13 to "CLIENT_DISCONNECT",
        14 to "SESSION_UPDATE",
        15 to "MEDIA_SINK_WANTS",

        11 to "CLIENTS_CONNECT",
        18 to "CLIENT_FLAGS",
        20 to "CLIENT_PLATFORM",
        21 to "DAVE_PREPARE_TRANSITION",
        22 to "DAVE_EXECUTE_TRANSITION",
        23 to "DAVE_TRANSITION_READY",
        24 to "DAVE_PREPARE_EPOCH",
        25 to "DAVE_MLS_EXTERNAL_SENDER",
        26 to "DAVE_MLS_KEY_PACKAGE",
        27 to "DAVE_MLS_PROPOSALS",
        28 to "DAVE_MLS_COMMIT_WELCOME",
        29 to "DAVE_MLS_ANNOUNCE_COMMIT_TRANSITION",
        30 to "DAVE_MLS_WELCOME",
        31 to "DAVE_MLS_INVALID_COMMIT_WELCOME",
    )

    fun getNameOf(i: Int) = nameLookup[i] ?: "Unknown ($i)"
}
