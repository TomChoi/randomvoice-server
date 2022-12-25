package com.randomvoice.signaling

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "A media with this name already exists")
class MediaAlreadyExistsException : RuntimeException() {

}

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "A media with this name does not exists")
class MediaNotFoundException : RuntimeException() {

}
