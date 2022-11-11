package com.randomvoice.signaling

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class SignalingConfiguration : WebSocketConfigurer {
    private val allowedOrigin: String = ""

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(SignalingHandler(), "/socket1",).setAllowedOrigins(allowedOrigin)
    }
}