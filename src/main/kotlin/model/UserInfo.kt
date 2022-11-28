package model

import org.springframework.web.socket.WebSocketSession

data class UserInfo(
    val userId: String,
    val session: WebSocketSession,
    var readyToMatching: Boolean = false
) {
}