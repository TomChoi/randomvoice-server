package com.randomvoice.signaling

import com.fasterxml.jackson.databind.ObjectMapper
import model.SignalData
import model.SignalType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SignalingHandler : TextWebSocketHandler() {

    private val sessions: MutableList<WebSocketSession> = mutableListOf()
    private val sessionMap = ConcurrentHashMap<String, WebSocketSession>()
    private val map1 = ObjectMapper()
    private val log1: Logger = LoggerFactory.getLogger(SignalingHandler::class.java)

    @Override
    private fun handleBinaryMessage(session: WebSocketSession, message: TextMessage) {
        val msg1: String = message.payload
        val sigData: SignalData = map1.readValue(msg1, SignalData::class.java)
        log1.debug("Receive message from client:", msg1)

        when {
            sigData.type.equals(SignalType.Login.toString(), ignoreCase = true) -> handleLogin(session)
            sigData.type.equals(SignalType.NewMember.toString(), ignoreCase = true) -> handleNewMember(sigData)
            sigData.type.equals(SignalType.Offer.toString(), ignoreCase = true) -> handleOffer(sigData)
            sigData.type.equals(SignalType.Answer.toString(), ignoreCase = true) -> handleAnswer(sigData)
            sigData.type.equals(SignalType.Ice.toString(), ignoreCase = true) -> handleIce(sigData)
        }
    }

    private fun handleLogin(session: WebSocketSession) {
        val sigResp = SignalData()
        val userId = UUID.randomUUID().toString()
        sigResp.userId = "signaling"
        sigResp.type = SignalType.UserId.toString()
        sigResp.data = userId
        sessionMap[userId] = session
        session.sendMessage(TextMessage(map1.writeValueAsString(sigResp)))
    }

    private fun handleNewMember(data: SignalData) {
        sessionMap.values.forEach { session ->
            val sigResp2 = SignalData()
            sigResp2.userId = data.userId
            sigResp2.type = SignalType.NewMember.toString()
            if (session.isOpen) {
                log1.debug("Sending New Member from", data.userId)
                sendMessage(session, sigResp2)
            }
        }
    }

    private fun handleOffer(data: SignalData) {
        val sigResp = SignalData()
        sigResp.userId = data.userId
        sigResp.type = SignalType.Offer.toString()
        sigResp.data = data.data
        sigResp.toUid = data.toUid
        sessionMap[data.userId]?.let { session ->
            sendMessage(session, sigResp)
        }
    }

    private fun handleAnswer(data: SignalData) {
        val sigResp = SignalData()
        sigResp.userId = data.userId
        sigResp.type = SignalType.Answer.toString()
        sigResp.data = data.data
        sigResp.toUid = data.toUid
        sessionMap[data.userId]?.let { session ->
            sendMessage(session, sigResp)
        }
    }

    private fun handleIce(data: SignalData) {
        val sigResp = SignalData()
        sigResp.userId = data.userId
        sigResp.type = SignalType.Ice.toString()
        sigResp.data = data.data
        sigResp.toUid = data.toUid
        sessionMap[data.userId]?.let { session ->
            sendMessage(session, sigResp)
        }
    }

    private fun sendMessage(session: WebSocketSession, data: SignalData) {
        try {
            session.sendMessage(TextMessage(map1.writeValueAsString(data)))
        } catch (e: Exception) {
            log1.error("Error Sending message:", e)
        }
    }
}