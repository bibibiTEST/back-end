package com.example.rc4_backend.controller;

import com.example.rc4_backend.pojo.ChatMessage;
import com.example.rc4_backend.pojo.EmbedderResponse;
import com.example.rc4_backend.tool.DataEmbedder;
import com.example.rc4_backend.tool.ThreadPoolTool;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api")
public class rc4Controller {
    private volatile HashMap<Integer, ChatMessage> receiveIdToFileMap = new HashMap<>();
    private rc4Controller() {
        receiveIdToFileMap.put(1, ChatMessage.builder()
                .content(" ").blockOrder(null).build());
        receiveIdToFileMap.put(2, ChatMessage.builder()
                .content(" ").blockOrder(null).build());
    }
    /**
     * 发送图片
     * @param file
     * @param userId
     * @return
     */
    @PostMapping(value = "/upload")
    public String upload(MultipartFile file, @RequestParam("userId") int userId) {
        System.out.println("收到请求:" + file.getName() + " :" + userId);
        if (userId != 1 && userId != 2) {
            return "当前id有误";
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
             response = future.get(10, TimeUnit.SECONDS);
        } catch (RuntimeException | InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return "get方法获取结果异常";
        }
        if (response == null) {
            return "调用嵌入算法未正确返回结果";
        }
        int id = userId == 1 ? 2 : 1;
        ChatMessage chatMessage = ChatMessage.builder()
                .content(response.getEmbedderdFilePath())
                .blockOrder(response.getBlockOrder()).build();
        receiveIdToFileMap.put(id, chatMessage);
        return "嵌入任务已提交";
    }

    /**
     * 接收图片
     * @param userId
     * @return
     */
    @GetMapping(value = "/messages")
    public ChatMessage messages(@RequestParam("userId") int userId) {
        if (userId != 1 && userId != 2) {
            throw new IllegalArgumentException("当前id有误");
        }
        System.out.println(userId);
        if (receiveIdToFileMap.containsKey(userId)) {
            System.out.println("已拿到url："+ receiveIdToFileMap.get(userId).getContent());
            return receiveIdToFileMap.get(userId);
        } else {
            System.out.println("该用户没收到消息");
        }
        return null;
    }

}
