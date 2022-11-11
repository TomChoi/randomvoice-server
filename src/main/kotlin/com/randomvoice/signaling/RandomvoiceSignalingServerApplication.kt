package com.randomvoice.signaling

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RandomvoiceSignalingServerApplication

fun main(args: Array<String>) {
	runApplication<RandomvoiceSignalingServerApplication>(*args)
}
