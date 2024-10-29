package com.github.tartaricacid.netmusic.api.pojo

import com.google.common.collect.Lists
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.StringUtils
import java.util.function.Consumer

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
        private val artists: List<Artist>? = null

        val transName: String
            get() {
                if (transNames == null || transNames.isEmpty()) {
                    return StringUtils.EMPTY
                }
                return transNames[0]
            }

        fun needVip(): Boolean {
            return fee == 1
        }

        fun getArtists(): List<String?> {
            if (artists == null || artists.isEmpty()) {
                return emptyList<String>()
            }

            val artistNames: MutableList<String?> = Lists.newArrayList()
            artists.forEach(Consumer { artist: Artist -> artistNames.add(artist.name) })
            return artistNames
        }
    }

    private class Artist {
        @SerializedName("name")
        val name: String? = null
    }

    fun getSong(): Song? {
        if (song!!.isEmpty()) {
            return null
        }

        return song[0]
    }
}