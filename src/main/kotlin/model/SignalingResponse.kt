package model

sealed class SignalingResponse<T>(
    open val type: String,
    open val from: String,
    open val to: String,
    open val tx: String,
    open val payload: T
) {

    data class Login(
        override val type: String = SignalingType.Login.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val payload: Payload,
    ) : SignalingResponse<Login.Payload>(type, from, to, tx, payload) {
        data class Payload(
            val data: String,
        )
    }

    data class NewMember(
        override val type: String = SignalingType.NewMember.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val payload: Payload,
    ) : SignalingResponse<NewMember.Payload>(type, from, to, tx, payload) {
        data class Payload(
            val data: String,
        )
    }

    data class Offer(
        override val type: String = SignalingType.Offer.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val payload: Payload,
    ) : SignalingResponse<Offer.Payload>(type, from, to, tx, payload) {
        data class Payload(
            val sdp: String,
        )
    }

    data class Answer(
        override val type: String = SignalingType.Answer.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val payload: Payload,
    ) : SignalingResponse<Answer.Payload>(type, from, to, tx, payload) {
        data class Payload(
            val sdp: String,
        )
    }

    data class Ack(
        override val type: String = SignalingType.Ack.toString(),
        override val from: String,
        override val to: String,
        override val tx: String,
        override val payload: Payload,
    ) : SignalingResponse<Ack.Payload>(type, from, to, tx, payload) {
        object Payload
    }
}