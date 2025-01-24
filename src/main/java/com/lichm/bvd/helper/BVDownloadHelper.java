package com.lichm.bvd.helper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * @Author: LiChengMing
 * @Date: Created in 15:59 2024/12/10
 * @Description:
 */
public class BVDownloadHelper {
    static Logger log = Logger.getLogger(BVDownloadHelper.class);

    /**
     * 获取连接
     *
     * @param spec
     * @return HttpURLConnection
     */
    public static HttpURLConnection getConn(String spec){
        HttpURLConnection conn = null;
        try {
            URL url = new URL(spec);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36");
            conn.setRequestProperty("referer", "https://www.bilibili.com");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }

    /**
     * 获取编码
     *
     * @param conn
     * @return
     */
    public static String getCharset(HttpURLConnection conn) {
        String contentType = conn.getHeaderField("Content-Type");
        String charset = "UTF-8";
        if (contentType != null) {
            String[] parts = contentType.split(";");
            for (String part : parts) {
                part = part.trim();
                if (part.startsWith("charset=")) {
                    charset = part.substring("charset=".length());
                    break;
                }

            }
        }
        return charset;
    }

    /**
     * 获取输入流
     *
     * @param conn
     * @param charset
     * @return String
     */
    public static String getReaderIoInfo(HttpURLConnection conn, String charset) {
        //是否压缩
        String contentEncoding = conn.getContentEncoding();
        BufferedReader in;
        StringBuilder response = null;
        try {
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                in = new BufferedReader(new InputStreamReader(new GZIPInputStream(conn.getInputStream()), charset));
            } else if ("deflate".equalsIgnoreCase(contentEncoding)) {
                in = new BufferedReader(new InputStreamReader(new InflaterInputStream(conn.getInputStream()), charset));
            } else {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
            }

            String inputLine;
            response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response.toString();
    }

    /**
     * 获取视频标题
     *
     * @param response
     * @return
     */
    public static String getTitle(String response) {
        Matcher titleMatcher = Pattern.compile("<h1 data-title=\"(.*?)\" title=\"(.*?)\" class=\"video-title special-text-indent\" data-v-1be0114a>").matcher(response);
        String title = "";
        if (titleMatcher.find()) {
            if (!Objects.equals(titleMatcher.group(), "")) {
                String[] titleSplit = titleMatcher.group().split(" ");
                for (String value : titleSplit) {
                    if (value.contains("title=")) {
                        String[] split = value.split("=");
                        if (split.length > 1) {
                            title = split[1].replace("\"", "");
                            //System.out.println(title);
                            break;
                        }
                    }
                }
            }
        }
        return title;
    }

    /**
     * 获取源url
     *
     * @param response
     * @param memberName
     * @return
     */
    public static String getOriginUrl(String response, String memberName) {
        String regex = "<script>window.__playinfo__=(.*?)</script>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(response);
        String group = "";
        String url = "";
        if (matcher.find()) {
            group = matcher.group();
        }

        if (!Objects.equals(group, "")) {
            Gson gson = new Gson();
            JsonObject fromJson = gson.fromJson(group.replace("<script>window.__playinfo__=", "").replace("</script>", ""), JsonObject.class);

            url = fromJson.getAsJsonObject("data").getAsJsonObject("dash").getAsJsonArray(memberName).get(0).getAsJsonObject().asMap().get("baseUrl").getAsString();
        }
        return url;
    }

    /**
     * 下载
     * @param title
     * @param videoOriginConn
     * @param fileType
     * @return
     * @throws IOException
     */
    public static String BVDownload(String title, HttpURLConnection videoOriginConn, String fileType) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(videoOriginConn.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream(System.getProperty("user.dir")+"\\"+"BVD"+ title +fileType);
        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = bufferedInputStream.read(buffer, 0, 1024)) != -1){
            fileOutputStream.write(buffer, 0, count);
        }
        return System.getProperty("user.dir")+"\\"+"BVD"+title +fileType;
    }

    /**
     * 合并
     * @param videoFileDownloadUrl
     * @param audioFileDownloadUrl
     * @param outputFile
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static String merge(String videoFileDownloadUrl, String audioFileDownloadUrl, String outputFile) throws IOException, InterruptedException {
        String command = String.format("ffmpeg -i %s -i %s -c:v copy -c:a aac -strict experimental %s", videoFileDownloadUrl, audioFileDownloadUrl, outputFile);

        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine())!=null){
            log.info(line);
        }

        int exitCode = process.waitFor();
        if (exitCode == 0){
            return "success";
        }else {
            return "fail";
        }
    }
}
