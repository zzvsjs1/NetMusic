package com.github.tartaricacid.netmusic.api

import com.github.tartaricacid.netmusic.api.EncryptUtils.encryptedParam
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class WebApi(val requestPropertyData: Map<String, String>) {

    companion object {
        const val TYPE_SONG: Int = 1
        const val TYPE_ALBUM: Int = 10
        const val TYPE_SINGER: Int = 100
        const val TYPE_PLAY_LIST: Int = 1000
        const val TYPE_USER: Int = 1002
        const val TYPE_RADIO: Int = 1009
    }

    @Throws(Exception::class)
    fun search(key: String, size: Long, page: Long, type: Int): String {
        val url = "http://music.163.com/weapi/cloudsearch/get/web?csrf_token="
        val param =
            "{\"s\":\"" + key + "\",\"type\":" + type + ",\"offset\":" + (page - 1) * size + ",\"limit\":" + size + ",\"total\":true,\"csrf_token\":\"\"}"
        val encrypt = encryptedParam(param)
        return NetWorker.post(url, encrypt, requestPropertyData)
    }

    @Throws(Exception::class)
    fun album(albumId: Long): String {
        val url = "http://music.163.com/weapi/v1/album/$albumId?id=$albumId&offset=0&total=true&limit=12"
        val param = "{\"album_id\":$albumId,\"csrf_token\":\"\"}"
        val encrypt = encryptedParam(param)
        return NetWorker.post(url, encrypt, requestPropertyData)
    }

    @Throws(IOException::class)
    fun song(songId: Long): String {
        val url = "http://music.163.com/api/song/detail/?id=$songId&ids=%5B$songId%5D"
        return NetWorker.get(url, requestPropertyData)
    }

    @Throws(IOException::class)
    fun songs(vararg songIds: Long): String {
        val ids = StringUtils.deleteWhitespace(songIds.contentToString())
        val url = "http://music.163.com/api/song/detail/?ids=" + URLEncoder.encode(ids, StandardCharsets.UTF_8)
        return NetWorker.get(url, requestPropertyData)
    }

    @Throws(IOException::class)
    fun lyric(songId: Long): String {
        val url = "http://music.163.com/api/song/lyric/?id=$songId&lv=-1&kv=-1&tv=-1"
        return NetWorker.get(url, requestPropertyData)
    }

    @Throws(Exception::class)
    fun mp3(quality: Long, vararg songIds: Long): String {
        val url = "http://music.163.com/weapi/song/enhance/player/url?csrf_token="
        val param = "{\"ids\":" + songIds.contentToString() + ",\"br\":" + quality + ",\"csrf_token\":\"\"}"
        val encrypt = encryptedParam(param)
        return NetWorker.post(url, encrypt, requestPropertyData)
    }

    @Throws(Exception::class)
    fun songComments(songId: Long, size: Long, page: Long): String {
        val url = "http://music.163.com/weapi/v1/resource/comments/R_SO_4_$songId?csrf_token="
        val param =
            "{\"id\":" + songId + ",\"offset\":" + (page - 1) * size + ",\"limit\":" + size + ",\"total\":true ,\"csrf_token\":\"\"}"
        val encrypt = encryptedParam(param)
        return NetWorker.post(url, encrypt, requestPropertyData)
    }

    @Throws(Exception::class)
    fun listComments(listId: Long, size: Long, page: Long): String {
        val url = "http://music.163.com/weapi/v1/resource/comments/A_PL_0_$listId?csrf_token="
        val param =
            "{\"id\":" + listId + ",\"offset\":" + (page - 1) * size + ",\"limit\":" + size + ",\"total\":true ,\"csrf_token\":\"\"}"
        val encrypt = encryptedParam(param)
        return NetWorker.post(url, encrypt, requestPropertyData)
    }

    @Throws(Exception::class)
    fun list(listId: Long): String {
        val url = "http://music.163.com/weapi/v3/playlist/detail?csrf_token="
        val param = "{\"id\":$listId,\"csrf_token\":\"\"}"
        val encrypt = encryptedParam(param)
        return NetWorker.post(url, encrypt, requestPropertyData)
    }

    @Throws(Exception::class)
    fun userAllList(userId: Long, size: Long, page: Long): String {
        val url = "http://music.163.com/api/user/playlist/?uid=$userId&offset=0&total=true&limit=1000"
        return NetWorker.get(url, requestPropertyData)
    }

    @Throws(Exception::class)
    fun getRedirectMusicUrl(musicId: Long): String? {
        val url = String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", musicId)
        return NetWorker.getRedirectUrl(url, requestPropertyData)
    }

}