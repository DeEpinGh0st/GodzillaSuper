//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package util.http;

import core.ApplicationContext;
import core.shell.ShellEntity;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.ConnectionPool;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.MediaType;
import okio.BufferedSink;

public class Http {
    private static final HostnameVerifier hostnameVerifier = new TrustAnyHostnameVerifier();
    private final Proxy proxy;
    private final ShellEntity shellContext;
    private CookieManager cookieManager;
    private URI uri;
    public String requestMethod = "POST";
    private boolean chunked = false;
    private int chunklen = 1024;
    private SSLSocketFactory sslSocketFactory;

    public Http(ShellEntity shellContext) {
        this.shellContext = shellContext;
        this.proxy = ApplicationContext.getProxy(this.shellContext);
        this.setSslSocketFactory(shellContext.getClientCertAuthSSLSocketFactory());
    }

    public HttpResponse SendHttpConn(String urlString, String method, LinkedHashMap<String, LinkedList<String>> header, byte[] requestData, int connTimeOut, int readTimeOut, Proxy proxy) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpConn = (HttpURLConnection)url.openConnection(proxy);
            httpConn.setInstanceFollowRedirects(false);
            if (!"GET".equalsIgnoreCase(method)) {
                if (this.isChunked()) {
                    httpConn.setChunkedStreamingMode(this.chunklen);
                } else {
                    httpConn.setFixedLengthStreamingMode(requestData.length);
                }
            }

            if (httpConn instanceof HttpsURLConnection) {
                ((HttpsURLConnection)httpConn).setHostnameVerifier(hostnameVerifier);
                if (this.sslSocketFactory != null) {
                    ((HttpsURLConnection)httpConn).setSSLSocketFactory(this.sslSocketFactory);
                }
            }

            httpConn.setDoInput(true);
            httpConn.setDoOutput(!"GET".equalsIgnoreCase(method));
            if (connTimeOut > 0) {
                httpConn.setConnectTimeout(connTimeOut);
            }

            if (readTimeOut > 0) {
                httpConn.setReadTimeout(readTimeOut);
            }

            httpConn.setRequestMethod(method.toUpperCase());
            LinkedHashMap<String, LinkedList<String>> headerMap = (LinkedHashMap)ApplicationContext.getGloballHttpHeaderX().clone();
            headerMap.putAll(header);
            addHttpHeader(httpConn, headerMap);
            if (httpConn.getDoOutput()) {
                httpConn.getOutputStream().write(requestData);
                httpConn.getOutputStream().flush();
            }

            return new HttpResponse(httpConn, this.shellContext, this.getUri(), this.getCookieManager());
        } catch (Exception var11) {
            var11.printStackTrace();
            throw new RuntimeException(var11.getMessage());
        }
    }

    public HttpResponse SendOkHttpConn(String urlString, String method, LinkedHashMap<String, LinkedList<String>> header, byte[] requestData, int connTimeOut, int readTimeOut, Proxy proxy) {
        try {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            clientBuilder.followRedirects(false);
            clientBuilder.followSslRedirects(false);
            clientBuilder.hostnameVerifier(hostnameVerifier);
            if (proxy != null) {
                clientBuilder.proxy(proxy);
            }
            URL url = new URL(urlString);
            String host = url.getHost();
            int port = url.getPort();
            if (port != -1) {
                host += ":" + port;
            }
            X509TrustManager trustManager = new miTM();
            SSLSocketFactory socketFactory = this.sslSocketFactory;
            if (socketFactory == null) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init((KeyManager[])null, new TrustManager[]{trustManager}, new SecureRandom());
                socketFactory = sslContext.getSocketFactory();
            }
            clientBuilder.sslSocketFactory(socketFactory, trustManager);

            if (connTimeOut > 0) {
                clientBuilder.connectTimeout((long)connTimeOut, TimeUnit.MILLISECONDS);
            }
            if (readTimeOut > 0) {
                clientBuilder.readTimeout((long)readTimeOut, TimeUnit.MILLISECONDS);
                clientBuilder.writeTimeout((long)readTimeOut, TimeUnit.MILLISECONDS);
            }
            // close the TCP connection right after each request (no keep-alive reuse)
            clientBuilder.connectionPool(new ConnectionPool(0, 1, TimeUnit.MILLISECONDS));

            LinkedHashMap<String, LinkedList<String>> headerMap = (LinkedHashMap)ApplicationContext.getGloballHttpHeaderX().clone();
            headerMap.putAll(header);

            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder.addHeader("Host", host);
            requestBuilder.url(urlString);

            Iterator<String> names = headerMap.keySet().iterator();
            while(names.hasNext()) {
                String name = (String)names.next();
                if (name != null && !name.equalsIgnoreCase("Content-Length")) {
                    LinkedList<String> values = (LinkedList)headerMap.get(name);
                    if (values != null && !values.isEmpty()) {
                        requestBuilder.header(name, (String)values.getFirst());
                        Iterator<String> it = values.iterator();
                        if (it.hasNext()) {
                            it.next();
                        }
                        while(it.hasNext()) {
                            requestBuilder.addHeader(name, (String)it.next());
                        }
                    }
                }
            }

            String methodUpper = method == null ? "GET" : method.toUpperCase();
            if ("GET".equalsIgnoreCase(methodUpper)) {
                requestBuilder.get();
            } else {
                String contentTypeHeader = getFirstHeaderValueIgnoreCase(headerMap, "Content-Type");
                MediaType mediaType = contentTypeHeader != null && !contentTypeHeader.isEmpty() ? MediaType.parse(contentTypeHeader) : null;
                final byte[] data = requestData != null ? requestData : new byte[0];
                final boolean chunkedBody = this.isChunked();
                RequestBody body = new RequestBody() {
                    public MediaType contentType() {
                        return mediaType;
                    }

                    public long contentLength() {
                        return chunkedBody ? -1L : (long)data.length;
                    }

                    public void writeTo(BufferedSink sink) throws java.io.IOException {
                        if (data.length > 0) {
                            sink.write(data);
                        }
                    }
                };
                requestBuilder.method(methodUpper, body);
            }

            OkHttpClient client = clientBuilder.build();
            try (Response response = client.newCall(requestBuilder.build()).execute()) {
                Headers respHeaders = response.headers();
                LinkedHashMap<String, List<String>> responseHeaderMap = new LinkedHashMap();
                LinkedList<String> statusList = new LinkedList();
                statusList.add("HTTP/1.1 " + response.code() + " " + response.message());
                responseHeaderMap.put((String)null, statusList);
                Set<String> headerNames = respHeaders.names();
                Iterator<String> hn = headerNames.iterator();

                while(hn.hasNext()) {
                    String name = (String)hn.next();
                    List<String> values = respHeaders.values(name);
                    responseHeaderMap.put(name, values);
                }

                ResponseBody responseBody = response.body();
                byte[] result = responseBody != null ? responseBody.bytes() : new byte[0];
                return new HttpResponse(result, responseHeaderMap, response.code(), this.shellContext, this.getUri(), this.getCookieManager(), response.header("Content-Encoding"));
            } finally {
                client.dispatcher().executorService().shutdown();
                client.connectionPool().evictAll();
            }
        } catch (Exception var24) {
            var24.printStackTrace();
            throw new RuntimeException(var24.getMessage());
        }
    }

    public static void addHttpHeader(HttpURLConnection connection, LinkedHashMap<String, LinkedList<String>> headerMap) {
        if (headerMap != null) {
            Iterator<String> names = headerMap.keySet().iterator();
            String name = "";

            while(names.hasNext()) {
                name = (String)names.next();
                Iterator<String> values = ((LinkedList)headerMap.get(name)).iterator();
                connection.setRequestProperty(name, (String)values.next());

                while(values.hasNext()) {
                    try {
                        connection.addRequestProperty(name, (String)values.next());
                    } catch (Exception var6) {
                    }
                }
            }
        }

    }

    public static void addHttpHeader(HttpURLConnection connection, Map<String, String> headerMap) {
        if (headerMap != null) {
            Iterator<String> names = headerMap.keySet().iterator();
            String name = "";

            while(names.hasNext()) {
                name = (String)names.next();
                connection.setRequestProperty(name, (String)headerMap.get(name));
            }
        }

    }

    private static String getFirstHeaderValueIgnoreCase(LinkedHashMap<String, LinkedList<String>> headerMap, String headerName) {
        if (headerMap == null) {
            return null;
        } else {
            Iterator<String> names = headerMap.keySet().iterator();
            while(names.hasNext()) {
                String name = (String)names.next();
                if (name != null && name.equalsIgnoreCase(headerName)) {
                    LinkedList<String> values = (LinkedList)headerMap.get(name);
                    return values != null && !values.isEmpty() ? (String)values.getFirst() : null;
                }
            }

            return null;
        }
    }

    private static void trustAllHttpsCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[1];
            TrustManager tm = new miTM();
            trustAllCerts[0] = tm;
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init((KeyManager[])null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            SSLContext sc2 = SSLContext.getInstance("TLS");
            sc2.init((KeyManager[])null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc2.getSocketFactory());
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public synchronized URI getUri() {
        if (this.uri == null) {
            try {
                this.uri = URI.create(this.shellContext.getUrl());
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }

        return this.uri;
    }

    public synchronized CookieManager getCookieManager() {
        if (this.cookieManager == null) {
            this.cookieManager = new CookieManager();

            try {
                LinkedList<String> cookieList = (LinkedList)this.shellContext.getHeaders().get("Cookie");
                if (cookieList == null) {
                    cookieList = (LinkedList)this.shellContext.getHeaders().get("cookie");
                }

                if (cookieList != null) {
                    String cookieStr = (String)cookieList.getFirst();
                    if (cookieStr != null) {
                        String[] cookies = cookieStr.split(";");
                        String[] var4 = cookies;
                        int var5 = cookies.length;

                        for(int var6 = 0; var6 < var5; ++var6) {
                            String cookieStr2 = var4[var6];
                            String[] cookieAtt = cookieStr2.split("=");
                            if (cookieAtt.length == 2) {
                                HttpCookie httpCookie = new HttpCookie(cookieAtt[0], cookieAtt[1]);
                                this.cookieManager.getCookieStore().add(this.getUri(), httpCookie);
                            }
                        }
                    }
                }
            } catch (Exception var10) {
                var10.printStackTrace();
            }
        }

        return this.cookieManager;
    }

    public boolean isChunked() {
        return this.chunked;
    }

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    public int getChunkLen() {
        return this.chunklen;
    }

    public void setChunkLen(int chunklen) {
        if (chunklen < 1024) {
            this.chunklen = 1024;
        } else {
            this.chunklen = chunklen;
        }
    }

    public SSLSocketFactory getSslSocketFactory() {
        return this.sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    static {
        trustAllHttpsCertificates();
    }

    public static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public TrustAnyHostnameVerifier() {
        }

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public static class miTM extends X509ExtendedTrustManager implements TrustManager, X509TrustManager {
        public miTM() {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        }
    }
}
