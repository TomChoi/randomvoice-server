package model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

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
}