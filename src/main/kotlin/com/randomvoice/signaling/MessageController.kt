package com.randomvoice.signaling

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("message")
class MessageController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("all")
    fun index(): List<Message> {

        logger.info("Tom!!!!!")
        return listOf(
            Message("1", "Hello!"),
            Message("2", "Tom!")
        )
    }
}

data class Message(val id: String, val text: String)