package com.example.rc4_backend.controller;

import com.example.rc4_backend.pojo.DecodeResponse;
import com.example.rc4_backend.pojo.EmbedderResponse;
import com.example.rc4_backend.tool.DataEmbedder;
import com.example.rc4_backend.tool.ThreadPoolTool;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/rc4")
public class rc4Controller {
    private volatile HashMap<Integer, DecodeResponse> receiveIdToFileMap = new HashMap<>();
    /**
     * 发送图片
     * @param file
     * @param sendId
     * @return
     */
    @PostMapping(value = "/sendMsg")
    public String sendMsg(MultipartFile file, @RequestParam("sendId") int sendId) {
        if (sendId != 1 && sendId != 2) {
            return "当前id有误";
        }
        if (file == null) {
            return "图片为空";
        }
        ThreadPoolExecutor executor = ThreadPoolTool.embedderThreadPool;
        DataEmbedder dataEmbedder = DataEmbedder.getInstance();
        CompletableFuture<EmbedderResponse> future = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return dataEmbedder.Embed(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }, executor);
        EmbedderResponse response;
        try {
             response = future.get(2, TimeUnit.SECONDS);
        } catch (RuntimeException | InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return "get方法获取结果异常";
        }
        if (response == null) {
            return "调用嵌入算法未正确返回结果";
        }
        int id = sendId == 1 ? 2 : 1;
        DecodeResponse decodeResponse = DecodeResponse.builder()
                .file(response.getEmbedderdFilePath())
                .blockOrder(response.getBlockOrder()).build();
        receiveIdToFileMap.put(id, decodeResponse);
        return "嵌入任务已提交";
    }

    /**
     * 接收图片
     * @param receivedId
     * @return
     */
    @GetMapping(value = "/receiveMsg")
    public DecodeResponse receiveMsg(@RequestParam("receivedId") int receivedId) {
        if (receivedId != 1 && receivedId != 2) {
            throw new IllegalArgumentException("当前id有误");
        }
        if (receiveIdToFileMap.containsKey(receivedId)) {
            return receiveIdToFileMap.get(receivedId);
        }
        return null;
    }


    @PostMapping(value = "/downloadImage")
    public void downloadImage() {
        //TODO 下载图片
    }

}
