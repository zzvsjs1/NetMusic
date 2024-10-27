package com.github.tartaricacid.netmusic.client.audio

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.api.NetWorker
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.network.chat.Component
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

@OnlyIn(Dist.CLIENT)
object MusicPlayManager {

    private const val ERROR_404 = "http://music.163.com/404"
    private const val MUSIC_163_URL = "https://music.163.com/"
    private const val LOCAL_FILE_PROTOCOL = "file"

    @JvmStatic
    fun play(url: String?, songName: String, sound: (URL) -> SoundInstance) {
        var urlVar = url

        if (urlVar!!.startsWith(MUSIC_163_URL)) {
            try {
                urlVar = NetWorker.getRedirectUrl(urlVar, NetMusic.NET_EASE_WEB_API.requestPropertyData)
            } catch (e: IOException) {
                NetMusic.LOGGER.error(e)
            }
        }

        if (urlVar != null && urlVar != ERROR_404) {
            playMusic(urlVar, songName, sound)
        }
    }

    @JvmStatic
    private fun playMusic(url: String, songName: String, sound: (URL) -> SoundInstance) {
        val urlFinal: URL

        try {
            urlFinal = URI(url).toURL()
            // If local file
            if (urlFinal.protocol.equals(LOCAL_FILE_PROTOCOL, ignoreCase = true)) {
                val file = File(urlFinal.toURI())
                if (!file.exists()) {
                    NetMusic.LOGGER.info("File not found: {}", url)
                    return
                }
            }

            Minecraft.getInstance().submitAsync {
                try {
                    Minecraft.getInstance().apply {
                        soundManager.play(sound(urlFinal))
                        gui.setNowPlaying(Component.literal(songName))
                    }
                } catch (e: Exception) {
                    NetMusic.LOGGER.error("Error playing sound for URL '{}': {}", urlFinal, e.message)
                }
            }
        } catch (e: MalformedURLException) {
            NetMusic.LOGGER.error(e)
        } catch (e: URISyntaxException) {
            NetMusic.LOGGER.error(e)
        }
    }
}