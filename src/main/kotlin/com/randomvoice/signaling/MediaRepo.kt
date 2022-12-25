package com.randomvoice.signaling

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MediaRepo : JpaRepository<Media, Long> {
    fun findByName(name: String): Media
    fun existsByName(name: String): Boolean
    @Query(nativeQuery = true, value = "SELECT name FROM media")
    fun getAllEntryNames(): List<String>
}