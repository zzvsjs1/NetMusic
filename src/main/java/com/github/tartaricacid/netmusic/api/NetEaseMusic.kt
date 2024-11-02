package com.github.tartaricacid.netmusic.api

import org.apache.commons.lang3.StringUtils
import java.util.HashMap

/**
 * @author 内个球
 */
class NetEaseMusic {

    private val requestPropertyData = hashMapOf<String, String>()

    constructor() {
        init()
    }

    constructor(cookie: String) {
        init()
        requestPropertyData["Cookie"] = cookie
    }

    private fun init() {
        requestPropertyData["Host"] = "music.163.com"
        requestPropertyData["Origin"] = "http://music.163.com"
        requestPropertyData["Referer"] = "http://music.163.com/"
        requestPropertyData["Content-Type"] = "application/x-www-form-urlencoded"
        requestPropertyData["User-Agent"] = StringUtils.joinWith(
            "\u0020",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64)",
            "AppleWebKit/537.36 (KHTML, like Gecko)",
            "Chrome/81.0.4044.138",
            "Safari/537.36"
        )
    }

    val api: WebApi
        get() = WebApi(requestPropertyData)
}