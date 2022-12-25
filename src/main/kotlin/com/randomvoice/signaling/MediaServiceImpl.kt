package com.randomvoice.signaling

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class MediaServiceImpl(
    private val repo: MediaRepo
) : MediaService {

    override fun getMedia(name: String): Media {
        if (!repo.existsByName(name)) {
            throw MediaNotFoundException()
        }
        return repo.findByName(name)
    }

    override fun saveMedia(file: MultipartFile, name: String) {
        if (repo.existsByName(name)) {
            throw MediaAlreadyExistsException()
        }
        val newFile = Media(name = name, data = file.bytes)
        repo.save(newFile)
    }

    override fun getAllMediaNames(): List<String> {
        return repo.getAllEntryNames()
    }
}