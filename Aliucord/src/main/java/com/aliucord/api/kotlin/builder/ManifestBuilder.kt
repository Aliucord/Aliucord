package com.aliucord.api.kotlin.builder

class ManifestBuilder {

    var changelog: String? = null
    var changelogMedia: String = "https://cdn.discordapp.com/banners/169256939211980800/eda024c8f40a45c88265a176f0926bea.jpg?size=2048"
    var description: String = ""
    var updateUrl: String? = null
    var version: String = "1.0.0"

    val authors = mutableListOf<Author>()

    fun addAuthor(builder: Author.() -> Unit) {
        authors.add(Author().apply(builder))
    }

    class Author {
        var name: String? = null
        var id: Long = 0
    }

}