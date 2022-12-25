package com.randomvoice.signaling

import javax.persistence.*

@Entity
class Media (

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(unique = true)
    val name: String,

    @Lob val data: ByteArray
)