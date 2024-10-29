package com.github.tartaricacid.netmusic.api.pojo

import com.google.common.collect.Lists
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.StringUtils
import java.util.function.Consumer

class NetEaseMusicList {
    @SerializedName(value = "result", alternate = ["playlist"])
    val playList: PlayList? = null

    @SerializedName("code")
    val code: Int = 0

    class Track {
        @SerializedName("id")
        val id: Long = 0

        @SerializedName("name")
        val name: String? = null

        @SerializedName(value = "artists", alternate = ["ar"])
        private val artists: List<Artist>? = null

        @SerializedName(value = "album", alternate = ["al"])
        val album: Album? = null

        @SerializedName(value = "duration", alternate = ["dt"])
        val duration: Int = 0

        @SerializedName(value = "transNames", alternate = ["tns"])
        private val transNames: List<String>? = null

        @SerializedName("fee")
        private val fee = 0

        fun getArtists(): List<String?> {
            if (artists == null || artists.isEmpty()) {
                return emptyList<String>()
            }
            val artistNames: MutableList<String?> = Lists.newArrayList()
            artists.forEach(Consumer { artist: Artist -> artistNames.add(artist.name) })
            return artistNames
        }

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
    }

    private class Creator {
        @SerializedName("nickname")
        private val nickname: String? = null
    }

    private class Artist {
        @SerializedName("name")
        val name: String? = null
    }

    class Album {
        @SerializedName("name")
        private val name: String? = null
    }

    class PlayList {
        @SerializedName("name")
        val name: String? = null

        @SerializedName("trackCount")
        val trackCount: Int = 0

        @SerializedName("playCount")
        val playCount: Int = 0

        @SerializedName("creator")
        private val creator: Creator? = null

        @SerializedName("createTime")
        private val createTime: Long = 0

        @SerializedName("subscribedCount")
        val subscribedCount: Int = 0

        @SerializedName("shareCount")
        val shareCount: Int = 0

        @SerializedName("tags")
        private val tags: List<String> = Lists.newArrayList()

        @SerializedName("description")
        val description: String = ""

        @SerializedName("tracks")
        val tracks: List<Track>? = null

        @SerializedName("trackIds")
        val trackIds: List<TrackId>? = null

        inner class TrackId {
            @SerializedName("id")
            val id: Long = 0
        }
    }
}