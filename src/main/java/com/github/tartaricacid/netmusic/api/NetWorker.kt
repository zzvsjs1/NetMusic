package com.github.tartaricacid.netmusic.api

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.config.GeneralConfig
import org.apache.commons.lang3.StringUtils
import java.io.*
import java.net.*
import java.nio.charset.StandardCharsets

/**
 * @author 内个球
 */
object NetWorker {

//    @Throws(IOException::class)
//    fun get(url: String, requestPropertyData: Map<String, String?>): String {
//        val result = StringBuilder()
//        val urlConnect: URL
//
//        try {
//            urlConnect = URI(url).toURL()
//            val connection = urlConnect.openConnection(proxyFromConfig)
//
//            val keys: Collection<String> = requestPropertyData.keys
//            for (key in keys) {
//                val `val` = requestPropertyData[key]
//                connection.setRequestProperty(key, `val`)
//            }
//
//            connection.connectTimeout = 12000
//            connection.doInput = true
//
//            BufferedReader(
//                InputStreamReader(
//                    connection.getInputStream(),
//                    StandardCharsets.UTF_8
//                )
//            ).use { bufferedReader ->
//                var line: String?
//                while ((bufferedReader.readLine().also { line = it }) != null) {
//                    result.append(line)
//                }
//            }
//        } catch (e: IOException) {
//            NetMusic.LOGGER.error(e)
//            throw e
//        } finally {
//        }
//
//        return result.toString()
//    }

    @Throws(IOException::class)
    fun get(url: String, requestProperties: Map<String, String?>, proxy: Proxy? = null): String {
        val result = StringBuilder()

        try {
            val urlConnection = URI(url).toURL().openConnection(proxy) as HttpURLConnection

            // Set request properties
            requestProperties.forEach { (key, value) ->
                value?.let {
                    urlConnection.setRequestProperty(key, it)
                }
            }

            // Configure connection settings
            urlConnection.connectTimeout = 12_000 // 12 seconds
            urlConnection.readTimeout = 12_000 // Optional: set read timeout
            urlConnection.requestMethod = "GET"
            urlConnection.doInput = true

            // Connect and read the response
            urlConnection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { bufferedReader ->
                bufferedReader.forEachLine { line ->
                    result.appendLine(line)
                }
            }
        } catch (e: IOException) {
            NetMusic.LOGGER.error("Failed to GET from URL: $url with properties: $requestProperties", e)
            throw e
        }

        return result.toString()
    }

    @Throws(IOException::class)
    fun getRedirectUrl(url: String, requestPropertyData: Map<String, String?>): String? {
        val urlConnect = URI(url).toURL()
        val connection = urlConnect.openConnection(proxyFromConfig) as HttpURLConnection
        val keys: Collection<String> = requestPropertyData.keys
        for (key in keys) {
            val `val` = requestPropertyData[key]
            connection.setRequestProperty(key, `val`)
        }

        connection.connectTimeout = 3000
        connection.readTimeout = 5000
        return connection.getHeaderField("Location")
    }

    /**
     * Sends an HTTP POST request to the specified [url] with the given [param] and [requestProperties].
     *
     * This function constructs a [URL] from the provided string, sets up the connection with the specified
     * request properties, sends the POST parameters, and retrieves the response.
     *
     * @param url the [String] representing the endpoint to send the POST request to.
     * @param param the POST parameters as a [String]. Can be `null` if no parameters are to be sent.
     * @param requestProperties a [Map] containing request header keys and their corresponding values.
     * @param proxy an optional [Proxy] to route the connection through. Defaults to `null`.
     * @return the response from the server as a [String].
     * @throws IOException if an I/O exception occurs during the process.
     */
    @Throws(IOException::class)
    fun post(
        url: String,
        param: String?,
        requestProperties: Map<String, String?>,
        proxy: Proxy? = null
    ): String {
        val result = StringBuilder()

        try {
            // Create URL and open connection
            val urlConnection = URI(url).toURL().openConnection(proxy) as HttpURLConnection

            // Set request properties
            requestProperties.forEach { (key, value) ->
                value?.let {
                    urlConnection.setRequestProperty(key, it)
                }
            }

            // Configure connection settings
            urlConnection.apply {
                connectTimeout = 12_000 // 12 seconds
                readTimeout = 12_000 // 12 seconds
                requestMethod = "POST"
                doOutput = true
                doInput = true
            }

            // Write POST parameters if present
            param?.let {
                urlConnection.outputStream.use { outputStream ->
                    OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                        writer.write(it)
                        writer.flush()
                    }
                }
            }

            // Connect and handle the response
            val responseCode = urlConnection.responseCode
            if (responseCode in 200..299) {
                urlConnection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { bufferedReader ->
                    bufferedReader.forEachLine { line ->
                        result.appendLine(line)
                    }
                }
            } else {
                // Optionally read the error stream for more information
                val errorStream = urlConnection.errorStream
                val errorMessage = errorStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }
                NetMusic.LOGGER.error("HTTP POST request failed with response code $responseCode and message: $errorMessage")
                throw IOException("HTTP POST request failed with response code $responseCode")
            }

        } catch (e: IOException) {
            NetMusic.LOGGER.error("Failed to POST to URL: $url with parameters: $param and properties: $requestProperties", e)
            throw e
        }

        return result.toString()
    }

//    private val proxyFromConfig: Proxy
//        get() {
//            val proxyType = GeneralConfig.PROXY_TYPE!!.get()
//            val proxyAddress = GeneralConfig.PROXY_ADDRESS!!.get()
//            if (proxyType == Proxy.Type.DIRECT || StringUtils.isBlank(proxyAddress)) {
//                return Proxy.NO_PROXY
//            }
//
//            val split = proxyAddress.split(":".toRegex(), limit = 2).toTypedArray()
//            if (split.size != 2) {
//                return Proxy.NO_PROXY
//            }
//            return Proxy(proxyType, InetSocketAddress(split[0], split[1].toInt()))
//        }

    /**
     * Retrieves the proxy configuration based on the application's general settings.
     *
     * This property checks the configured proxy type and address. If the proxy type is `DIRECT`
     * or the proxy address is blank or improperly formatted, it defaults to `Proxy.NO_PROXY`.
     * Otherwise, it parses the proxy address to create a [Proxy] instance.
     *
     * @return A [Proxy] instance based on the configuration or `Proxy.NO_PROXY` if no proxy is set.
     */
    private val proxyFromConfig: Proxy
        get() {
            val proxyType = GeneralConfig.PROXY_TYPE?.get() ?: Proxy.Type.DIRECT
            val proxyAddress = GeneralConfig.PROXY_ADDRESS?.get()

            if (proxyType == Proxy.Type.DIRECT || proxyAddress.isNullOrBlank()) {
                return Proxy.NO_PROXY
            }

            val split = proxyAddress.split(":", limit = 2).map { it.trim() }
            if (split.size != 2) {
                NetMusic.LOGGER.warn("Invalid proxy address format: '$proxyAddress'. Using NO_PROXY.")
                return Proxy.NO_PROXY
            }

            val (host, portStr) = split
            if (host.isEmpty() || portStr.isEmpty()) {
                NetMusic.LOGGER.warn("Invalid proxy address format: '$proxyAddress'. Using NO_PROXY.")
                return Proxy.NO_PROXY
            }

            val port = portStr.toIntOrNull()
            if (port == null || port !in 1..65535) {
                NetMusic.LOGGER.warn("Invalid proxy port: '$portStr'. Using NO_PROXY.")
                return Proxy.NO_PROXY
            }

            return try {
                Proxy(proxyType, InetSocketAddress(host, port))
            } catch (e: IllegalArgumentException) {
                NetMusic.LOGGER.warn("Failed to create proxy with host: '$host' and port: $port. Using NO_PROXY.")
                Proxy.NO_PROXY
            }
        }
}