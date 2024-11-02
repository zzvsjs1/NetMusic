package com.github.tartaricacid.netmusic.client.config

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.api.ExtraMusicList
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong
import com.github.tartaricacid.netmusic.item.ItemMusicCD.SongInfo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import org.apache.commons.io.FileUtils
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.min

object MusicListManage {

    private const val MAX_NUM = 100

    private val GSON = Gson()

    private val CONFIG_DIR: Path = Paths.get("config").resolve("net_music")

    private val CONFIG_FILE: Path = CONFIG_DIR.resolve("music.json")

    var SONGS: MutableList<SongInfo> = mutableListOf()

    @Throws(IOException::class)
    fun loadConfigSongs() {
        if (!Files.isDirectory(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR)
        }

        val file = CONFIG_FILE.toFile()
        var stream: InputStream? = null

        if (Files.exists(CONFIG_FILE)) {
            stream = Files.newInputStream(file.toPath())
        } else {
            val res = ResourceLocation(NetMusic.MOD_ID, "music.json")
            val optional = Minecraft.getInstance().resourceManager.getResource(res)
            if (optional.isPresent) {
                stream = optional.get().open()
            }
        }

        if (stream != null) {
            SONGS = GSON.fromJson(
                InputStreamReader(stream, StandardCharsets.UTF_8),
                object : TypeToken<List<SongInfo?>?>() {}.type
            )
        }
    }

    @Throws(Exception::class)
    fun get163Song(id: Long): SongInfo {
        val pojo = GSON.fromJson(
            NetMusic.NET_EASE_WEB_API.song(id),
            NetEaseMusicSong::class.java
        )

        return SongInfo(pojo)
    }

    @Throws(Exception::class)
    fun add163List(id: Long) {
        if (!Files.isDirectory(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR)
        }

        val pojo = GSON.fromJson(
            NetMusic.NET_EASE_WEB_API.list(id),
            NetEaseMusicList::class.java
        )

        val count = pojo.playList!!.tracks.size
        val size = min(pojo.playList.trackIds.size.toDouble(), MAX_NUM.toDouble())
            .toInt()

        // 获取额外歌曲
        if (count < size) {
            val ids = LongArray(size - count)
            for (i in count until size) {
                ids[i - count] = pojo.playList.trackIds[i].id
            }

            val extraTrackInfo = NetMusic.NET_EASE_WEB_API.songs(*ids)
            val extra = GSON.fromJson(
                extraTrackInfo,
                ExtraMusicList::class.java
            )

            pojo.playList.tracks.addAll(extra.tracks)
        }

        SONGS.clear()

        for (track in pojo.playList.tracks) {
            SONGS.add(SongInfo(track))
        }

        val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        FileUtils.write(CONFIG_FILE.toFile(), gson.toJson(SONGS), StandardCharsets.UTF_8)
    }
}