package com.github.tartaricacid.netmusic.api;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * @author 内个球
 */
public class NetWorker {

    public static String get(String url, Map<String, String> requestPropertyData) throws IOException {
        StringBuilder result = new StringBuilder();
        URL urlConnect;

        try {
            urlConnect = new URL(url);
            URLConnection connection = urlConnect.openConnection(getProxyFromConfig());

            Collection<String> keys = requestPropertyData.keySet();
            for (String key : keys) {
                String val = requestPropertyData.get(key);
                connection.setRequestProperty(key, val);
            }

            connection.setConnectTimeout(12000);
            connection.setDoInput(true);

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                    StandardCharsets.UTF_8))
            ) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
            }
        } catch (IOException e) {
            NetMusic.LOGGER.error(e);
            throw e;
        } finally {

        }

        return result.toString();
    }

    //    public static String get(String url, Map<String, String> requestPropertyData) throws IOException {
    //        StringBuilder result = new StringBuilder();
    //        HttpURLConnection connection = null;
    //
    //        try {
    //            URL urlConnect = new URL(url);
    //            connection = (HttpURLConnection) urlConnect.openConnection(getProxyFromConfig());
    //            connection.setRequestMethod("GET");
    //            connection.setConnectTimeout(12000);
    //            connection.setReadTimeout(12000);
    //            connection.setDoInput(true);
    //
    //            // Set request properties
    //            for (Map.Entry<String, String> entry : requestPropertyData.entrySet()) {
    //                connection.setRequestProperty(entry.getKey(), entry.getValue());
    //            }
    //
    //            // Handle the response
    //            int responseCode = connection.getResponseCode();
    //            if (responseCode >= 200 && responseCode < 300) {
    //                Charset charset = StandardCharsets.UTF_8; // Default
    //                String contentType = connection.getContentType();
    //                if (contentType != null) {
    //                    for (String param : contentType.replace(" ", "").split(";")) {
    //                        if (param.startsWith("charset=")) {
    //                            charset = Charset.forName(param.split("=", 2)[1]);
    //                            break;
    //                        }
    //                    }
    //                }
    //
    //                try (BufferedReader bufferedReader = new BufferedReader(
    //                        new InputStreamReader(connection.getInputStream(), charset))) {
    //                    String line;
    //                    while ((line = bufferedReader.readLine()) != null) {
    //                        result.append(line);
    //                    }
    //                }
    //            } else {
    //                // Handle error responses
    //                throw new IOException("HTTP error code: " + responseCode);
    //            }
    //
    //        } finally {
    //            if (connection != null) {
    //                connection.disconnect();
    //            }
    //        }
    //
    //        return result.toString();
    //    }

    @Nullable
    public static String getRedirectUrl(String url, Map<String, String> requestPropertyData) throws IOException {
        URL urlConnect = new URL(url);
        URLConnection connection = urlConnect.openConnection(getProxyFromConfig());
        Collection<String> keys = requestPropertyData.keySet();
        for (String key : keys) {
            String val = requestPropertyData.get(key);
            connection.setRequestProperty(key, val);
        }

        connection.setConnectTimeout(3_000);
        connection.setReadTimeout(5_000);
        return connection.getHeaderField("Location");
    }

    public static String post(String url, String param, Map<String, String> requestPropertyData) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader bufferedReader;
        PrintWriter printWriter;

        URL urlConnect = new URL(url);
        URLConnection connection = urlConnect.openConnection(getProxyFromConfig());

        Collection<String> keys = requestPropertyData.keySet();
        for (String key : keys) {
            String val = requestPropertyData.get(key);
            connection.setRequestProperty(key, val);
        }
        connection.setConnectTimeout(12000);
        connection.setDoOutput(true);
        connection.setDoInput(true);

        printWriter = new PrintWriter(connection.getOutputStream());
        printWriter.print(param);
        printWriter.flush();

        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }

        bufferedReader.close();
        printWriter.close();

        return result.toString();
    }

    private static Proxy getProxyFromConfig() {
        Proxy.Type proxyType = GeneralConfig.PROXY_TYPE.get();
        String proxyAddress = GeneralConfig.PROXY_ADDRESS.get();
        if (proxyType == Proxy.Type.DIRECT || StringUtils.isBlank(proxyAddress)) {
            return Proxy.NO_PROXY;
        }

        String[] split = proxyAddress.split(":", 2);
        if (split.length != 2) {
            return Proxy.NO_PROXY;
        }
        return new Proxy(proxyType, new InetSocketAddress(split[0], Integer.parseInt(split[1])));
    }
}
