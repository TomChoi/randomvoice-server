package model

import kotlin.Error

sealed class SignalingResponse<T>(
    open val type: String,
    open val from: String,
    open val to: String,
    open val tx: String,
    open val error: Error? = null,
    open val payload: T? = null,
) {

    data class Error(
        val code: Int,
        val reason: String
    )

    data class Login(
        override val type: String = SignalingType.Login.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val error: Error? = null,
        override val payload: Payload? = null
    ) : SignalingResponse<Login.Payload>(type, from, to, tx, error, payload) {
        data class Payload(
            val data: String,
        )
    }

    data class NewMember(
        override val type: String = SignalingType.NewMember.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val error: Error? = null,
        override val payload: Payload? = null,
    ) : SignalingResponse<NewMember.Payload>(type, from, to, tx, error, payload) {
        data class Payload(
            val data: String,
        )
    }

    data class Offer(
        override val type: String = SignalingType.Offer.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val error: Error? = null,
        override val payload: Payload? = null,
    ) : SignalingResponse<Offer.Payload>(type, from, to, tx, error, payload) {
        data class Payload(
            val sdp: String,
        )
    }

    data class Answer(
        override val type: String = SignalingType.Answer.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val error: Error? = null,
        override val payload: Payload? = null,
    ) : SignalingResponse<Answer.Payload>(type, from, to, tx, error, payload) {
        data class Payload(
            val sdp: String,
        )
    }

    data class Ice(
        override val type: String = SignalingType.Ice.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val error: Error? = null,
        override val payload: Payload? = null,
    ) : SignalingResponse<Ice.Payload>(type, from, to, tx, error, payload) {
        data class Payload(
            val sdpMid: String,
            val sdpMLineIndex: Int,
            val sdp: String
        )
    }

    data class LogOut(
        override val type: String = SignalingType.Logout.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val error: Error? = null,
    ) : SignalingResponse<String>(type, from, to, tx, error)

    data class Ack(
        override val type: String = SignalingType.Ack.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val error: Error? = null,
    ) : SignalingResponse<String>(type, from, to, tx, error)
}