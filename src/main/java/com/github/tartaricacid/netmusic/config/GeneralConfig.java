package com.github.tartaricacid.netmusic.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.net.Proxy;

public class GeneralConfig {
    public static ForgeConfigSpec.BooleanValue ENABLE_STEREO;
    public static ForgeConfigSpec.EnumValue<Proxy.Type> PROXY_TYPE;
    public static ForgeConfigSpec.ConfigValue<String> PROXY_ADDRESS;

    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("general");

        builder.comment("Whether stereo playback is enabled");
        ENABLE_STEREO = builder.define("EnableStereo", true);

        builder.comment("Proxy Type, http and socks are supported");
        PROXY_TYPE = builder.defineEnum("ProxyType", Proxy.Type.DIRECT);

        builder.comment("Proxy Address, such as 127.0.0.1:1080, empty is no proxy");
        PROXY_ADDRESS = builder.define("ProxyAddress", "");

        builder.pop();
        return builder.build();
    }
}
