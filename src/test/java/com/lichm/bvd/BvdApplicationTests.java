package com.lichm.bvd;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

@SpringBootTest
class BvdApplicationTests {

    @Test
    void contextLoads() {

        try {
            URL url = new URL("https://www.bilibili.com/video/BV1BU4y1H7E3");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36");
            conn.setRequestProperty("referer","https://www.bilibili.com");

            //获取编码信息
            String contentType = conn.getHeaderField("Content-Type");
            String charset = "UTF-8";
            if (contentType != null){
                String[] parts = contentType.split(";");
                for (String part:parts) {
                    part = part.trim();
                    if (part.startsWith("charset=")){
                        charset = part.substring("charset=".length());
                        break;
                    }

                }
            }

            //是否压缩
            String contentEncoding = conn.getContentEncoding();
            BufferedReader in;
            if ("gzip".equalsIgnoreCase(contentEncoding)){
                in = new BufferedReader(new InputStreamReader(new GZIPInputStream(conn.getInputStream()) ,charset));
            }else if ("deflate".equalsIgnoreCase(contentEncoding)){
                in = new BufferedReader(new InputStreamReader(new InflaterInputStream(conn.getInputStream()) ,charset));
            }else {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream() ,charset));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null){
                response.append(inputLine);

            }

            in.close();
            //System.out.println(response);

            Matcher titleMatcher = Pattern.compile("<h1 data-title=\"(.*?)\" title=\"(.*?)\" class=\"video-title special-text-indent\" data-v-1be0114a>").matcher(response);
            String title = "";
            if (titleMatcher.find()) {
                if (!Objects.equals(titleMatcher.group(), "")) {
                    String[] titleSplit = titleMatcher.group().split(" ");
                    for (String value : titleSplit) {
                        if (value.contains("title=")) {
                            String[] split = value.split("=");
                            if (split.length > 1) {
                                title = split[1].replace("\"","");
                                System.out.println(title);
                                break;
                            }
                        }
                    }
                }
            }


            String regex = "<script>window.__playinfo__=(.*?)</script>";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(response);
            String group = "";
            if (matcher.find()){
                group = matcher.group();
                System.out.println(group);
            }

            if (!Objects.equals(group, "")){
                Gson gson = new Gson();
                JsonObject fromJson = gson.fromJson(group.replace("<script>window.__playinfo__=", "").replace("</script>", ""), JsonObject.class);
                System.out.println(fromJson);

                String videoUrl = fromJson.getAsJsonObject("data").getAsJsonObject("dash").getAsJsonArray("video").get(0).getAsJsonObject().asMap().get("baseUrl").getAsString();
                String audioUrl = fromJson.getAsJsonObject("data").getAsJsonObject("dash").getAsJsonArray("audio").get(0).getAsJsonObject().asMap().get("baseUrl").getAsString();
                System.out.println(videoUrl);
                System.out.println(audioUrl);

            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
