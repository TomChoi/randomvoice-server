package com.randomvoice.signaling

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import model.*
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
    private val roomMap = ConcurrentHashMap<String, RoomInfo>()
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
            val userId = payload.data
            userMap[userId] = UserInfo(userId, session)

            val sigResp = SignalingResponse.Ack(
                from = "",
                to = from,
                tx = tx,
            )
            log1.info("Send message to client:$sigResp")
            session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
        }
    }

    private inline fun <reified T> convertStringToClass(string: String): T? {
        return mapper.readValue<T>(string)
    }

    private fun createRoom(): RoomInfo {
        val id = UUID.randomUUID().toString()
        log1.info("createRoom (id: $id)")
        val room = RoomInfo(id, 2)
        roomMap[id] = room
        return room
    }

    private fun destroyRoom(roomId: String) {
        log1.info("destroyRoom (id: $roomId")
        roomMap.remove(roomId)
    }

    private fun findRoom(): RoomInfo {
        return roomMap.filter { it.value.participants.size < it.value.maxParticipant }
            .values.asSequence().shuffled().find { true } ?: createRoom()
    }

    @Synchronized
    private fun handleEnter(session: WebSocketSession, request: String) {

        convertStringToClass<SignalingRequest.NewMember>(request)?.apply {

            val myUserId = from
            val userInfo = userMap[myUserId]
            val sigResp = if (userInfo != null) {
                findRoom().also {
                    log1.info("find room size(${roomMap.size}): (id: ${it.id}, maxParticipant: ${it.maxParticipant}, participant: ${it.participants}")
                    it.participants.forEach { partnerUserInfo ->
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
                    userInfo.roomId = it.id
                    it.participants.add(userInfo)
                }
                SignalingResponse.Ack(from = "", to = from, tx = tx)
            } else {
                SignalingResponse.Ack(from = "", to = from, tx = tx,
                    error = SignalingResponse.Error(code = 1000, reason = "user not found"))
            }
            log1.info("Send message to client: $sigResp")
            session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
        }
    }

    @Synchronized
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

    @Synchronized
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

    @Synchronized
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
//                log1.info("Send message to client: $sigResp")
                target.session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
            }

            val sigResp = if (targetUserInfo == null) {
                SignalingResponse.Ack(from = "", to = from, tx = tx,
                    error = SignalingResponse.Error(code = 1000, reason = "user not found"))
            } else {
                SignalingResponse.Ack(from = "", to = from, tx = tx)
            }

//            log1.info("Send message to client: $sigResp")
            session.sendMessage(TextMessage(mapper.writeValueAsString(sigResp)))
        }
    }

    @Synchronized
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
                leaveRoom(userInfo)
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

    @Synchronized
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

    @Synchronized
    private fun leaveRoom(userInfo: UserInfo) {
        userInfo.roomId?.also { id ->
            roomMap[id]?.also { roomInfo ->
                roomInfo.participants.remove(userInfo)
                if (roomInfo.participants.size == 0) {
                    destroyRoom(id)
                }
            }
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        super.afterConnectionEstablished(session)
        session.handshakeHeaders["userId"]?.find { true }?.let { userId ->
            log1.info("WebSocket established userId: $userId")
            userMap[userId] = UserInfo(userId, session)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        super.afterConnectionClosed(session, status)
        session.handshakeHeaders["userId"]?.find { true }?.let { userId ->
            log1.info("WebSocket closed userId: $userId")
            userMap[userId]?.also {
                leaveRoom(it)
            }
            userMap.remove(userId)
        }
    }
}