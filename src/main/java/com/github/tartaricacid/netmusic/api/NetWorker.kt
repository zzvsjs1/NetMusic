package com.github.tartaricacid.netmusic.api

import com.github.tartaricacid.netmusic.NetMusic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import java.io.*
import java.net.*
import java.nio.charset.StandardCharsets

/**
 * NetWorker is a singleton object responsible for handling HTTP GET and POST requests.
 * It leverages Apache HttpClient for executing requests and supports both synchronous
 * and asynchronous operations using Kotlin coroutines.
 *
 * @author 内个球
 */
object NetWorker {

    private val connectionManager = PoolingHttpClientConnectionManager().apply {
        maxTotal = 10
        defaultMaxPerRoute = 10
    }

    private val HTTP_CLIENT = HttpClients.custom()
        .setConnectionManager(connectionManager)
        .setRetryHandler(DefaultHttpRequestRetryHandler(3, true))
        .build()

    private val DEFAULT_REQUEST_CONFIG = RequestConfig.custom()
        .setConnectTimeout(10_000) // 10 seconds
        .setConnectionRequestTimeout(10_000)  // 10 seconds
        .build()

    /**
     * Executes an asynchronous HTTP GET request.
     *
     * @param url The URL to send the GET request to.
     * @param requestProperties A map of request headers to include in the GET request.
     * @return The response body as a [String].
     * @throws NetWorkerException If the GET request fails.
     */
    suspend fun getAsync(url: String, requestProperties: Map<String, String?>): String = withContext(Dispatchers.IO) {
        get(url, requestProperties)
    }

    /**
     * Executes an asynchronous HTTP POST request.
     *
     * @param url The URL to send the POST request to.
     * @param param The request body as a [String]. Can be null.
     * @param requestProperties A map of request headers to include in the POST request.
     * @return The response body as a [String].
     * @throws NetWorkerException If the POST request fails.
     */
    suspend fun postAsync(url: String, param: String?, requestProperties: Map<String, String?>): String =
        withContext(Dispatchers.IO) {
            post(url, param, requestProperties)
        }

    /**
     * Executes a synchronous HTTP GET request.
     *
     * @param url The URL to send the GET request to.
     * @param requestProperties A map of request headers to include in the GET request.
     * @return The response body as a [String].
     * @throws NetWorkerException If the GET request fails or returns a non-2xx status code.
     */
    @Throws(NetWorkerException::class)
    fun get(url: String, requestProperties: Map<String, String?>): String {
        // Create the HTTP GET request
        val httpGet = HttpGet(url).apply {
            // Set headers from the requestProperties map
            requestProperties.forEach { (key, value) ->
                value?.let { setHeader(key, it) }
            }

            config = DEFAULT_REQUEST_CONFIG
        }

        try {
            // Execute the request using the HTTP client
            HTTP_CLIENT.execute(httpGet).use { response ->
                val statusCode = response.statusLine.statusCode
                val entity = response.entity ?: throw NetWorkerException("No response entity for GET request to $url")
                val responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8)

                if (statusCode in 200..299) {
                    return responseBody
                }

                throw NetWorkerException("GET request to $url returned status $statusCode: $responseBody")
            }
        } catch (e: IOException) {
            // Log the error with URL and request properties
            NetMusic.LOGGER.error("Failed to GET from URL: $url with properties: $requestProperties", e)
            throw NetWorkerException("GET request failed for URL: $url", e)
        }
    }


    /**
     * Retrieves the redirect URL from an HTTP response if a redirect is present.
     *
     * @param url The URL to send the GET request to.
     * @param requestProperties A map of request headers to include in the GET request.
     * @return The redirect URL as a [String] if a redirect status is received; otherwise, null.
     * @throws NetWorkerException If the request fails.
     */
    @Throws(NetWorkerException::class)
    fun getRedirectUrl(url: String, requestProperties: Map<String, String?>): String? {
        // Configure the request to not follow redirects automatically
        val requestConfig = RequestConfig.custom()
            .setRedirectsEnabled(false)
            .setConnectTimeout(3000)
            .setSocketTimeout(5000)
            .build()

        val httpGet = HttpGet(url).apply {
            requestProperties.forEach { (key, value) ->
                value?.let { setHeader(key, it) }
            }

            config = requestConfig
        }

        HTTP_CLIENT.execute(httpGet).use { response ->
            val statusCode = response.statusLine.statusCode
            if (statusCode in 300..399) {
                return response.getFirstHeader("Location")?.value
            }

            return null
        }
    }

    /**
     * Executes a synchronous HTTP POST request.
     *
     * @param url The URL to send the POST request to.
     * @param param The request body as a [String]. Can be null.
     * @param requestProperties A map of request headers to include in the POST request.
     * @param contentType The content type of the request body. Defaults to [ContentType.APPLICATION_JSON].
     * @return The response body as a [String]. Returns an empty string if the response has no entity.
     * @throws NetWorkerException If the POST request fails or returns a non-2xx status code.
     */
    @Throws(NetWorkerException::class)
    fun post(
        url: String,
        param: String?,
        requestProperties: Map<String, String?>,
        contentType: ContentType = ContentType.APPLICATION_JSON
    ): String {
        val requestConfig = RequestConfig.custom()
            // 12 seconds
            .setConnectTimeout(12_000)
            .setSocketTimeout(12_000)
            .build()

        // Create an HttpPost request with the specified URL
        val httpPost = HttpPost(url).apply {
            this.config = requestConfig
            // Set headers from requestPropertyData map
            requestProperties.forEach { (key, value) ->
                this.setHeader(key, value)
            }

            // Set the POST request body
            val entity = StringEntity(param, contentType)
            this.entity = entity
        }

        try {
            // Execute the POST request
            HTTP_CLIENT.execute(httpPost).use { response: CloseableHttpResponse ->
                // Check for a successful response (status code 2xx)
                val statusCode = response.statusLine.statusCode

                if (statusCode in 200..299) {
                    // Get the response entity
                    val entity = response.entity ?: return ""
                    // Convert the entity content to a String
                    return EntityUtils.toString(entity, StandardCharsets.UTF_8)
                }

                // Handle non-successful status codes as needed
                throw NetWorkerException("Unexpected response status: $statusCode for POST request to $url")
            }
        } catch (e: IOException) {
            // Log the error with URL and request properties
            NetMusic.LOGGER.error("Failed to POST to URL: $url with properties: $requestProperties", e)
            // Rethrow the exception to be handled by the caller
            throw NetWorkerException("POST request failed for URL: $url", e)
        }
    }

}