package com.github.tartaricacid.netmusic.config

import net.minecraftforge.common.ForgeConfigSpec
import java.net.Proxy

object GeneralConfig {

    @JvmField
    var ENABLE_STEREO: ForgeConfigSpec.BooleanValue? = null

    @JvmField
    var PROXY_TYPE: ForgeConfigSpec.EnumValue<Proxy.Type>? = null

    @JvmField
    var PROXY_ADDRESS: ForgeConfigSpec.ConfigValue<String>? = null

    fun buildConfig(): ForgeConfigSpec {
        val builder = ForgeConfigSpec.Builder()
        builder.push("general")

        builder.comment("Whether stereo playback is enabled")
        ENABLE_STEREO = builder.define("EnableStereo", true)

        builder.comment("Proxy Type, http and socks are supported")
        PROXY_TYPE = builder.defineEnum("ProxyType", Proxy.Type.DIRECT)

        builder.comment("Proxy Address, such as 127.0.0.1:1080, empty is no proxy")
        PROXY_ADDRESS = builder.define("ProxyAddress", "")

        builder.pop()
        return builder.build()
    }

}