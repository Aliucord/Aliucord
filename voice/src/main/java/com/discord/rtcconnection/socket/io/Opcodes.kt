package com.discord.rtcconnection.socket.io

object Opcodes {
    // Original opcodes
    @Suppress("unused") const val IDENTIFY = 0
    @Suppress("unused") const val SELECT_PROTOCOL = 1
    @Suppress("unused") const val READY = 2
    @Suppress("unused") const val HEARTBEAT = 3
    @Suppress("unused") const val SELECT_PROTOCOL_ACK = 4
    @Suppress("unused") const val SPEAKING = 5
    @Suppress("unused") const val HEARTBEAT_ACK = 6
    @Suppress("unused") const val RESUME = 7
    @Suppress("unused") const val HELLO = 8
    @Suppress("unused") const val RESUMED = 9
    @Suppress("unused") const val VIDEO = 12
    @Suppress("unused") const val CLIENT_DISCONNECT = 13
    @Suppress("unused") const val SESSION_UPDATE = 14
    @Suppress("unused") const val MEDIA_SINK_WANTS = 15
    // End original opcodes

    // New unused opcodes
    @Suppress("unused") const val CLIENTS_CONNECT = 11
    @Suppress("unused") const val CLIENT_FLAGS = 18
    @Suppress("unused") const val CLIENT_PLATFORM = 20

    // Dave opcodes, none of these should be unused!!
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
    fun friendly(i: Int) = "$i (${getNameOf(i)})"
}
