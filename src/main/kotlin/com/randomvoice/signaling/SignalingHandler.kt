package com.randomvoice.signaling

import com.fasterxml.jackson.databind.ObjectMapper
import model.SignalingType
import model.SignalingRequest
import model.SignalingResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SignalingHandler : TextWebSocketHandler() {

    private val sessionMap = ConcurrentHashMap<String, WebSocketSession>()
    private val matchingQueue = Collections.synchronizedList(mutableListOf<String>())
    private val map1 = ObjectMapper()
    private val log1: Logger = LoggerFactory.getLogger(SignalingHandler::class.java)

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val request: String = message.payload
        val header = map1.readValue(request, SignalingRequest::class.java)
        log1.info("Receive message from client:$request")

        when {
            header.type.equals(SignalingType.Login.toString(), ignoreCase = true) -> handleLogin(session, request)
            header.type.equals(SignalingType.Enter.toString(), ignoreCase = true) -> handleEnter(session, request)
            header.type.equals(SignalingType.Offer.toString(), ignoreCase = true) -> handleOffer(session, request)
            header.type.equals(SignalingType.Answer.toString(), ignoreCase = true) -> handleAnswer(session, request)
            header.type.equals(SignalingType.Ice.toString(), ignoreCase = true) -> handleIce(session, request)
            header.type.equals(SignalingType.Leave.toString(), ignoreCase = true) -> handleLeave(session, request)
        }
    }

    private fun handleLogin(session: WebSocketSession, request: String) {
        convertStringToClass<SignalingRequest.Login>(request)?.apply {

            val userId = getNewUserId()
            sessionMap[userId] = session

            val payload = SignalingResponse.Login.Payload(
                data = payload.data,
            )
            val sigResp = SignalingResponse.Login(
                from = "",
                to = userId,
                tx = tx,
                payload = payload
            )

            session.sendMessage(TextMessage(map1.writeValueAsString(sigResp)))
        }
    }

    private inline fun <reified T> convertStringToClass(string: String): T? {
        return map1.readValue(string, T::class.java)
    }

    private fun doRandomMatching(myId: String): String? =
        matchingQueue.filter { id -> id != myId && sessionMap[id]?.isOpen ?: false }
            .asSequence().shuffled().find { true }

    private fun getNewUserId(): String = UUID.randomUUID().toString()

    private fun handleEnter(session: WebSocketSession, request: String) {

        convertStringToClass<SignalingRequest.NewMember>(request)?.apply {

            val myUserId = from
            matchingQueue.add(myUserId)

            doRandomMatching(myUserId)?.also { partnerId ->
                val partner = sessionMap[partnerId]
                val payload = SignalingResponse.NewMember.Payload(
                    data = partnerId
                )
                val sigResp = SignalingResponse.NewMember(
                    from = partnerId,
                    to = myUserId,
                    tx = tx,
                    payload = payload
                )
                partner?.sendMessage(TextMessage(map1.writeValueAsString(sigResp)))
            }
        }
    }

    private fun handleOffer(session: WebSocketSession, request: String) {
        convertStringToClass<SignalingRequest.Offer>(request)?.apply {
            val payload = SignalingResponse.Offer.Payload(
                sdp = payload.sdp
            )
            val sigResp = SignalingResponse.Offer(
                from = from,
                to = to,
                tx = tx,
                payload = payload
            )
            session.sendMessage(TextMessage(map1.writeValueAsString(sigResp)))
        }
    }

    private fun handleAnswer(session: WebSocketSession, request: String) {
        convertStringToClass<SignalingRequest.Answer>(request)?.apply {
            val payload = SignalingResponse.Answer.Payload(
                sdp = payload.sdp
            )
            val sigResp = SignalingResponse.Answer(
                from = from,
                to = to,
                tx = tx,
                payload = payload
            )
            session.sendMessage(TextMessage(map1.writeValueAsString(sigResp)))
        }
    }

    private fun handleIce(session: WebSocketSession, request: String) {
//        val sigResp = SignalData()
//        sigResp.userId = data.userId
//        sigResp.type = SignalingType.Ice.toString()
//        sigResp.data = data.data
//        sigResp.toUid = data.toUid
//        sessionMap[data.userId]?.let { session ->
//            sendMessage(session, sigResp)
//        }
    }

    private fun handleLeave(session: WebSocketSession, request: String) {

    }

    private fun sendMessage(session: WebSocketSession, data: String) {
        try {
            session.sendMessage(TextMessage(data))
        } catch (e: Exception) {
            log1.error("Error Sending message:", e)
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        super.afterConnectionEstablished(session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        super.afterConnectionClosed(session, status)
    }
}