package com.example.traveldiary

import java.io.Serializable
data class Locations(
    val name: String = "",val address: String = "",
    val date: String = "",
    val notes: String = "",
    val documentId: String = "",
    val imageUrl: String = "",
    val userId: String = ""
) : Serializable {

    constructor(documentId: String) : this("", "", "", "", documentId, "", "")

    constructor() : this("", "", "", "", "", "", "") // No-argument constructor
}
