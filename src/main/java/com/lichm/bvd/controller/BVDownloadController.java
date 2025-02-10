package com.lichm.bvd.controller;

import com.lichm.bvd.helper.BVDownloadHelper;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.HttpURLConnection;

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
    public void bvdDownload(@RequestParam("url") String url){
        //url  "https://www.bilibili.com/video/BV1BU4y1H7E3"
        HttpURLConnection conn = BVDownloadHelper.getConn(url);
        log.info("建立连接成功");
        String charset = BVDownloadHelper.getCharset(conn);

        String readerIoInfo = BVDownloadHelper.getReaderIoInfo(conn, charset);

        String title = BVDownloadHelper.getTitle(readerIoInfo);

        String videoOriginUrl = BVDownloadHelper.getOriginUrl(readerIoInfo, "video");
        String audioOriginUrl = BVDownloadHelper.getOriginUrl(readerIoInfo, "audio");

        HttpURLConnection videoOriginConn = BVDownloadHelper.getConn(videoOriginUrl);
        HttpURLConnection audioOriginConn = BVDownloadHelper.getConn(audioOriginUrl);

        String videoFileDownloadUrl = BVDownloadHelper.BVDownload(title, videoOriginConn, ".mp4");
        log.info("video download success");
        String audioFileDownloadUrl = BVDownloadHelper.BVDownload(title, audioOriginConn, ".mp3");
        log.info("audio download success");
        String outputFile = System.getProperty("user.dir")+ title + ".mp4" ;
        log.info("=================================merging=================================");
        String merge = BVDownloadHelper.merge(videoFileDownloadUrl, audioFileDownloadUrl, outputFile);
        log.info("video merge" + merge);
        log.info("outputFile :" + outputFile);
    }

}
