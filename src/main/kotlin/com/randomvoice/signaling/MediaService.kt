package com.randomvoice.signaling

import org.springframework.web.multipart.MultipartFile
import java.io.IOException

interface MediaService {
    @Throws(IOException::class)
    fun getMedia(name: String): Media
    @Throws(IOException::class)
    fun saveMedia(file: MultipartFile, name: String)
    fun getAllMediaNames(): List<String>
}