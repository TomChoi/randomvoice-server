package com.randomvoice.signaling

import com.randomvoice.signaling.Message
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream

@RestController
@RequestMapping("media")
class MediaController(
    private val mediaService: MediaService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    @PostMapping()
    @Throws(IOException::class)
    fun saveMedia(@RequestParam("file") file: MultipartFile, @RequestParam("name") name: String): ResponseEntity<String> {
        logger.info("tom!!!!!!!saveMedia")
        mediaService.saveMedia(file, name)
        return ResponseEntity.ok("Media saved successfully.")
    }

    @GetMapping("{name}")
    fun getMediaByName(@PathVariable("name") name: String): ResponseEntity<ByteArrayResource> {
        logger.info("tom!!!!!!!getMediaByName")
        return ResponseEntity.ok(ByteArrayResource(mediaService.getMedia(name).data))
    }

    @GetMapping("all")
    fun getAllMediaNames(): ResponseEntity<List<String>> {
        logger.info("tom!!!!!!!GetMapping")
        return ResponseEntity.ok(mediaService.getAllMediaNames())
    }

    @GetMapping("bg")
    fun getBackgroundAudio(): ResponseEntity<StreamingResponseBody> {
        val file = File("/Users/tom/Desktop/bg_audio.mp3")
        if (!file.isFile) {
            return ResponseEntity.notFound().build()
        }

        val streamingResponseBody = StreamingResponseBody { outputStream ->
            try {
                val inputStream = FileInputStream(file)
                val bytes = ByteArray(1024)
                do {
                    val length = inputStream.read(bytes)
                    outputStream.write(bytes, 0, length)
                } while(length >= 0)
                inputStream.close()
                outputStream.flush()

            } catch(e: Exception) {
                logger.error("Exception while reading and streaming data $e")
            }
        }

        val responseHeaders = HttpHeaders()
        responseHeaders.add("Content-Type", "audio/mp3")
        responseHeaders.add("Content-Length", file.length().toString())

        return ResponseEntity.ok().headers(responseHeaders).body(streamingResponseBody)
    }
}