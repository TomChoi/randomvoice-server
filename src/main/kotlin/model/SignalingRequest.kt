package model

class SignalingRequest(val type: String) {

    data class Login(
        val from: String,
        val to: String,
        val tx: String,
        val payload: Payload,
    ) {
        data class Payload(
            val data: String,
        )
    }

    data class NewMember(
        val from: String,
        val to: String,
        val tx: String,
        val payload: Payload,
    ) {
        data class Payload(
            val data: String,
        )
    }

    data class Offer(
        val from: String,
        val to: String,
        val tx: String,
        val payload: Payload,
    ) {
        data class Payload(
            val sdp: String,
        )
    }

    data class Answer(
        val from: String,
        val to: String,
        val tx: String,
        val payload: Payload,
    ) {
        data class Payload(
            val sdp: String,
        )
    }

    data class Leave(
        val from: String,
        val to: String,
        val tx: String,
        val payload: Payload,
    ) {
        object Payload
    }

    data class Logout(
        val from: String,
        val to: String,
        val tx: String,
        val payload: Payload,
    ) {
        object Payload
    }

    data class Ice(
        val from: String,
        val to: String,
        val tx: String,
        val payload: Payload,
    ) {
        data class Payload(
            val sdpMid: String,
            val sdpMLineIndex: Int,
            val sdp: String
        )
    }

    data class KeepAlive(
        val from: String,
        val to: String,
        val tx: String,
        val payload: Payload,
    ) {
        object Payload
    }
}