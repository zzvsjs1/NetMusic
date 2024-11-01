package com.github.tartaricacid.netmusic.item

import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong
import com.github.tartaricacid.netmusic.init.InitItems
import com.google.common.collect.Lists
import com.google.gson.annotations.SerializedName
import net.minecraft.ChatFormatting
import net.minecraft.client.resources.language.I18n
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import org.apache.commons.lang3.StringUtils
import java.util.ArrayList
import java.util.function.Consumer

class ItemMusicCD : Item((Properties())) {

    override fun getName(stack: ItemStack): Component {
        val info = getSongInfo(stack)
        if (info != null) {
            var name = info.songName
            if (info.vip) {
                name = "$name §4§l[VIP]"
            }
            if (info.readOnly) {
                val readOnlyText = Component.translatable("tooltips.netmusic.cd.read_only")
                    .withStyle(ChatFormatting.YELLOW)
                return Component.literal(name).append(CommonComponents.SPACE).append(readOnlyText)
            }

            return Component.literal(name)
        }
        return super.getName(stack)
    }

    private fun getSongTime(songTime: Int): String {
        val min = songTime / 60
        val sec = songTime % 60
        val minStr = if (min <= 9) ("0$min") else ("" + min)
        val secStr = if (sec <= 9) ("0$sec") else ("" + sec)
        return I18n.get("tooltips.netmusic.cd.time.format", minStr, secStr)
    }


    override fun appendHoverText(
        stack: ItemStack,
        worldIn: Level?,
        tooltip: MutableList<Component>,
        flagIn: TooltipFlag
    ) {
        val info = getSongInfo(stack)
        val prefix = "§a▍ §7"
        val delimiter = ": "
        if (info != null) {
            if (StringUtils.isNoneBlank(info.transName)) {
                val text = prefix + I18n.get("tooltips.netmusic.cd.trans_name") + delimiter + "§6" + info.transName
                tooltip.add(Component.literal(text))
            }
            if (info.artists != null && !info.artists!!.isEmpty()) {
                val artistNames = StringUtils.join(info.artists, " | ")
                val text = prefix + I18n.get("tooltips.netmusic.cd.artists") + delimiter + "§3" + artistNames
                tooltip.add(Component.literal(text))
            }
            val text =
                prefix + I18n.get("tooltips.netmusic.cd.time") + delimiter + "§5" + getSongTime(info.songTime)
            tooltip.add(Component.literal(text))
        } else {
            tooltip.add(Component.translatable("tooltips.netmusic.cd.empty").withStyle(ChatFormatting.RED))
        }
    }

    class SongInfo {
        @JvmField
        @SerializedName("url")
        var songUrl: String? = null

        @JvmField
        @SerializedName("name")
        var songName: String? = null

        @JvmField
        @SerializedName("time_second")
        var songTime: Int = 0

        @JvmField
        @SerializedName("trans_name")
        var transName: String = StringUtils.EMPTY

        @JvmField
        @SerializedName("vip")
        var vip: Boolean = false

        @JvmField
        @SerializedName("read_only")
        var readOnly: Boolean = false

        @JvmField
        @SerializedName("artists")
        var artists: MutableList<String?>? = Lists.newArrayList()

        constructor(songUrl: String?, songName: String?, songTime: Int, readOnly: Boolean) {
            this.songUrl = songUrl
            this.songName = songName
            this.songTime = songTime
            this.readOnly = readOnly
        }

        constructor(pojo: NetEaseMusicSong) {
            val song = pojo.getSong()
            if (song != null) {
                this.songUrl = String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", song.id)
                this.songName = song.name
                this.songTime = song.duration / 1000
                this.transName = song.transName
                this.vip = song.needVip()
                this.artists = song.getArtists1().toMutableList()
            }
        }

        constructor(track: NetEaseMusicList.Track) {
            this.songUrl = String.format("https://music.163.com/song/media/outer/url?id=%d.mp3", track.id)
            this.songName = track.name
            this.songTime = track.duration / 1000
            this.transName = track.transName
            this.vip = track.needVip()
            this.artists = track.getArtists().toMutableList()
        }

        constructor(tag: CompoundTag) {
            this.songUrl = tag.getString("url")
            this.songName = tag.getString("name")
            this.songTime = tag.getInt("time")
            if (tag.contains("trans_name", Tag.TAG_STRING.toInt())) {
                this.transName = tag.getString("trans_name")
            }
            if (tag.contains("vip", Tag.TAG_BYTE.toInt())) {
                this.vip = tag.getBoolean("vip")
            }
            if (tag.contains("read_only", Tag.TAG_BYTE.toInt())) {
                this.readOnly = tag.getBoolean("read_only")
            }
            if (tag.contains("artists", Tag.TAG_LIST.toInt())) {
                val tagList = tag.getList("artists", Tag.TAG_STRING.toInt())
                this.artists = mutableListOf()
                tagList.forEach(Consumer { nbt: Tag -> (artists as ArrayList<String?>).add(nbt.asString) })
            }
        }

        companion object {

            @JvmStatic
            fun deserializeNBT(tag: CompoundTag): SongInfo {
                return SongInfo(tag)
            }

            @JvmStatic
            fun serializeNBT(info: SongInfo, tag: CompoundTag) {
                tag.putString("url", info.songUrl)
                tag.putString("name", info.songName)
                tag.putInt("time", info.songTime)
                if (StringUtils.isNoneBlank(info.transName)) {
                    tag.putString("trans_name", info.transName)
                }

                tag.putBoolean("vip", info.vip)
                tag.putBoolean("read_only", info.readOnly)
                if (info.artists != null && info.artists!!.isNotEmpty()) {
                    val nbt = ListTag()
                    info.artists!!.forEach(Consumer { name: String? -> nbt.add(StringTag.valueOf(name)) })
                    tag.put("artists", nbt)
                }
            }
        }
    }

    companion object {
        private const val SONG_INFO_TAG: String = "NetMusicSongInfo"

        @JvmStatic
        fun getSongInfo(stack: ItemStack): SongInfo? {
            if (stack.item === InitItems.MUSIC_CD.get()) {
                val tag = stack.tag
                if (tag != null && tag.contains(SONG_INFO_TAG, Tag.TAG_COMPOUND.toInt())) {
                    val infoTag = tag.getCompound(SONG_INFO_TAG)
                    return SongInfo.deserializeNBT(infoTag)
                }
            }

            return null
        }

        @JvmStatic
        fun setSongInfo(info: SongInfo, stack: ItemStack): ItemStack {
            if (stack.item === InitItems.MUSIC_CD.get()) {
                var tag = stack.tag
                if (tag == null) {
                    tag = CompoundTag()
                }

                val songInfoTag = CompoundTag()
                SongInfo.serializeNBT(info, songInfoTag)
                tag.put(SONG_INFO_TAG, songInfoTag)
                stack.tag = tag
            }

            return stack
        }
    }
}