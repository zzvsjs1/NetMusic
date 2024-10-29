package com.github.tartaricacid.netmusic.api

import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList
import com.google.common.collect.Lists
import com.google.gson.annotations.SerializedName

class ExtraMusicList {
    @SerializedName("code")
    val code: Int = 0

    @SerializedName("songs")
    val tracks: List<NetEaseMusicList.Track> = Lists.newArrayList()
}