package com.github.tartaricacid.netmusic.api.pojo

import com.google.gson.annotations.SerializedName

class NetEaseMusicSong {

    @SerializedName("code")
    val code: Int = 0

    @SerializedName("songs")
    private val song: List<Song>? = null

    class Song {
        @SerializedName("id")
        val id: Long = 0

        @SerializedName("name")
        val name: String? = null

        @SerializedName(value = "transNames", alternate = ["tns"])
        private val transNames: List<String>? = null

        @SerializedName(value = "duration", alternate = ["dt"])
        val duration: Int = 0

        @SerializedName("fee")
        private val fee = 0

        @SerializedName(value = "artists", alternate = ["ar"])
        val artists: List<Artist>? = null

        val transName: String
            get() {
                if (transNames.isNullOrEmpty()) {
                    return ""
                }

                return transNames[0]
            }

        fun needVip(): Boolean {
            return fee == 1
        }

        fun getArtists1(): List<String?> {
            if (artists.isNullOrEmpty()) {
                return emptyList<String>()
            }

            val artistNames: MutableList<String?> = mutableListOf()
            artists.forEach { artist: Artist -> artistNames.add(artist.name) }
            return artistNames
        }
    }

    class Artist {
        @SerializedName("name")
        val name: String? = null
    }

    fun getSong(): Song? {
        if (song.isNullOrEmpty()) {
            return null
        }

        return song[0]
    }
}