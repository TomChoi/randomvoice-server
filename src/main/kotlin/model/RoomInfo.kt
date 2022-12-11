package model

import java.util.UUID

data class RoomInfo(
    val id: String,
    val maxParticipant: Int,
    val participants: MutableList<UserInfo> = mutableListOf()
) {
}