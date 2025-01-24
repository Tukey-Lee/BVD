package com.lichm.bvd.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lichm.bvd.helper.BVDownloadHelper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
 * @Date: Created in 17:08 2024/12/9
 * @Description: 哔哩哔哩视频下载
 */

@RestController
@RequestMapping("/BVD")
public class BVDownloadController {
    static Logger log = Logger.getLogger(BVDownloadController.class);

    @GetMapping("/download")
    public void bvdDownload(@RequestParam("url") String url) throws IOException, InterruptedException {
        //url  "https://www.bilibili.com/video/BV1BU4y1H7E3"
        HttpURLConnection conn = BVDownloadHelper.getConn(url);
        log.info("建立连接成功");
        String charset = BVDownloadHelper.getCharset(conn);

        String readerIoInfo = BVDownloadHelper.getReaderIoInfo(conn, charset);

        String title = BVDownloadHelper.getTitle(readerIoInfo);

        String videoOriginUrl = BVDownloadHelper.getOriginUrl(readerIoInfo.toString(), "video");
        String audioOriginUrl = BVDownloadHelper.getOriginUrl(readerIoInfo.toString(), "audio");

        HttpURLConnection videoOriginConn = BVDownloadHelper.getConn(videoOriginUrl);
        HttpURLConnection audioOriginConn = BVDownloadHelper.getConn(audioOriginUrl);

        String videoFileDownloadUrl = BVDownloadHelper.BVDownload(title, videoOriginConn, ".mp4");
        String audioFileDownloadUrl = BVDownloadHelper.BVDownload(title, audioOriginConn, ".mp3");
        String outputFile = System.getProperty("user.dir")+ title + ".mp4" ;

        String merge = BVDownloadHelper.merge(videoFileDownloadUrl, audioFileDownloadUrl, outputFile);


    }




}
