package com.randomvoice.signaling

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import model.SignalingRequest
import model.SignalingType
import model.SignalingResponse
import model.UserInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SignalingHandler : TextWebSocketHandler() {

    private val userMap = ConcurrentHashMap<String, UserInfo>()
    private val mapper = ObjectMapper()
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val log1: Logger = LoggerFactory.getLogger(SignalingHandler::class.java)

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val request: String = message.payload
        val header = mapper.readValue<SignalingRequest>(request)
        val type: String = header.type
        log1.info("Receive message from client:$request")

        when {
            type.equals(SignalingType.Login.toString(), ignoreCase = true) -> handleLogin(session, request)
            type.equals(SignalingType.Enter.toString(), ignoreCase = true) -> handleEnter(session, request)
            type.equals(SignalingType.Offer.toString(), ignoreCase = true) -> handleOffer(session, request)
            type.equals(SignalingType.Answer.toString(), ignoreCase = true) -> handleAnswer(session, request)
            type.equals(SignalingType.Ice.toString(), ignoreCase = true) -> handleIce(session, request)
            type.equals(SignalingType.Leave.toString(), ignoreCase = true) -> handleLeave(session, request)
            type.equals(SignalingType.Logout.toString(), ignoreCase = true) -> handleLogout(session, request)
            type.equals(SignalingType.KeepAlive.toString(), ignoreCase = true) -> handleKeepAlive(session, request)
            else -> {
                log1.error("Request type not supported, type = $type")
            }
        }
    }

    private fun handleLogin(session: WebSocketSession, request: String) {
        convertStringToClass<SignalingRequest.Login>(request)?.apply {
            val userId = getNewUserId()
            userMap[userId] = UserInfo(userId, session, false)

            val payload = SignalingResponse.Login.Payload(
                data = userId,
            )
            val sigResp = SignalingResponse.Login(
                from = "",
                to = userId,
                tx = tx,
                payload = payload,
            )
            log1.info("Send message to client:$sigResp")
            session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
        }
    }

    private inline fun <reified T> convertStringToClass(string: String): T? {
        return mapper.readValue<T>(string)
    }

    @Synchronized
    private fun doRandomMatching(myUserInfo: UserInfo): UserInfo? {
        val partner = userMap.filter { it.value.readyToMatching && it.value.userId != myUserInfo.userId && it.value.session.isOpen }
            .values.asSequence().shuffled().find { true }

        if (partner == null) {
            log1.info("random matching failed, size = ${userMap.size}, myUserInfo = $myUserInfo")
        } else {
            log1.info("random matching success, partner userId = ${partner.userId} size = ${userMap.size}")
            myUserInfo.readyToMatching = false
            partner.readyToMatching = false
        }
        return partner
    }

    private fun getNewUserId(): String = UUID.randomUUID().toString()

    private fun handleEnter(session: WebSocketSession, request: String) {

        convertStringToClass<SignalingRequest.NewMember>(request)?.apply {

            val myUserId = from
            val userInfo = userMap[myUserId]
                userInfo?.also { myUserInfo ->
                myUserInfo.readyToMatching = true
                doRandomMatching(myUserInfo)?.also { partnerUserInfo ->

                    myUserInfo.readyToMatching = false
                    partnerUserInfo.readyToMatching = false

                    val payload = SignalingResponse.NewMember.Payload(
                        data = partnerUserInfo.userId
                    )
                    val sigResp = SignalingResponse.NewMember(
                        from = myUserId,
                        to = partnerUserInfo.userId,
                        payload = payload,
                    )
                    log1.info("Send message to client:$sigResp")
                    partnerUserInfo.session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
                }
            }
            val sigResp = if (userInfo == null) {
                SignalingResponse.Ack(from = "", to = from, tx = tx,
                    error = SignalingResponse.Error(code = 1000, reason = "user not found"))
            } else {
                SignalingResponse.Ack(from = "", to = from, tx = tx)
            }

            log1.info("Send message to client: $sigResp")
            session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
        }
    }

    private fun handleOffer(session: WebSocketSession, request: String) {
        convertStringToClass<SignalingRequest.Offer>(request)?.apply {

            val targetUserInfo = userMap[to]
            targetUserInfo?.also { target ->
                val payload = SignalingResponse.Offer.Payload(
                    sdp = payload.sdp
                )
                val sigResp = SignalingResponse.Offer(
                    from = from,
                    to = to,
                    payload = payload,
                )
                log1.info("Send message to client:$sigResp")
                target.session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
            }

            val sigResp = if (targetUserInfo == null) {
                SignalingResponse.Ack(from = "", to = from, tx = tx,
                    error = SignalingResponse.Error(code = 1000, reason = "user not found"))
            } else {
                SignalingResponse.Ack(from = "", to = from, tx = tx)
            }

            log1.info("Send message to client: $sigResp")
            session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
        }
    }

    private fun handleAnswer(session: WebSocketSession, request: String) {
        convertStringToClass<SignalingRequest.Answer>(request)?.apply {

            val targetUserInfo = userMap[to]
            targetUserInfo?.also { target ->
                val payload = SignalingResponse.Answer.Payload(
                    sdp = payload.sdp
                )
                val sigResp = SignalingResponse.Answer(
                    from = from,
                    to = to,
                    payload = payload,
                )
                log1.info("Send message to client:$sigResp")
                target.session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
            }

            val sigResp = if (targetUserInfo == null) {
                SignalingResponse.Ack(from = "", to = from, tx = tx,
                    error = SignalingResponse.Error(code = 1000, reason = "user not found"))
            } else {
                SignalingResponse.Ack(from = "", to = from, tx = tx)
            }

            log1.info("Send message to client: $sigResp")
            session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
        }
    }

    private fun handleIce(session: WebSocketSession, request: String) {
        convertStringToClass<SignalingRequest.Ice>(request)?.apply {

            val targetUserInfo = userMap[to]
            targetUserInfo?.also { target ->
                val payload = SignalingResponse.Ice.Payload(
                    sdpMid = payload.sdpMid,
                    sdpMLineIndex = payload.sdpMLineIndex,
                    sdp = payload.sdp
                )
                val sigResp = SignalingResponse.Ice(
                    from = from,
                    to = to,
                    payload = payload,
                )
                log1.info("Send message to client: $sigResp")
                target.session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
            }

            val sigResp = if (targetUserInfo == null) {
                SignalingResponse.Ack(from = "", to = from, tx = tx,
                    error = SignalingResponse.Error(code = 1000, reason = "user not found"))
            } else {
                SignalingResponse.Ack(from = "", to = from, tx = tx)
            }

            log1.info("Send message to client: $sigResp")
            session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
        }
    }

    private fun handleLeave(session: WebSocketSession, request: String) {
        convertStringToClass<SignalingRequest.Leave>(request)?.apply {

            val userInfo = userMap[from]
            val sigResp = if (userInfo == null) {
                SignalingResponse.Ack(
                    from = "",
                    to = from,
                    tx = tx,
                    error = SignalingResponse.Error(code = 1000, reason= "user not found")
                )
            } else {
                userInfo.readyToMatching = false
                SignalingResponse.Ack(
                    from = "",
                    to = from,
                    tx = tx,
                )
            }
            log1.info("Send message to client:$sigResp")
            session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
        }
    }

    private fun handleLogout(session: WebSocketSession, request: String) {

        convertStringToClass<SignalingRequest.Logout>(request)?.apply {

            val userInfo = userMap[from]
            val sigResp = if (userInfo == null) {
                SignalingResponse.Ack(
                    from = "",
                    to = from,
                    tx = tx,
                    error = SignalingResponse.Error(code = 1000, reason= "user not found")
                )
            } else {
                userMap.remove(from)
                SignalingResponse.Ack(
                    from = "",
                    to = from,
                    tx = tx,
                )
            }
            log1.info("Send message to client:$sigResp")
            session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
        }
    }

    private fun handleKeepAlive(session: WebSocketSession, request: String) {

        convertStringToClass<SignalingRequest.KeepAlive>(request)?.apply {

            val userInfo = userMap[from]
            val sigResp = if (userInfo == null) {
                SignalingResponse.Ack(
                    from = "",
                    to = from,
                    tx = tx,
                    error = SignalingResponse.Error(code = 1000, reason = "user not found")
                )
            } else {

                SignalingResponse.Ack(
                    from = "",
                    to = from,
                    tx = tx
                )
            }
            log1.info("Send message to client: $sigResp")
            session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
        }
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