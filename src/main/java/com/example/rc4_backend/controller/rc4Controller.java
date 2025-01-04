package com.example.rc4_backend.controller;

import com.example.rc4_backend.pojo.DecodeResponse;
import com.example.rc4_backend.pojo.EmbedderResponse;
import com.example.rc4_backend.tool.DataEmbedder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/api/rc4")
public class rc4Controller {
    static HashMap<Integer, DecodeResponse> receiveIdToFileMap = new HashMap<>();
    // List<Integer> idList = new ArrayList<>();
//    DataEmbedder dataEmbedder = DataEmbedder.getInstance();
    List<DataEmbedder> dataEmbedders;
    private int embedderCnt = 0;
    @Autowired
    private ExecutorService executorService; // 线程池
    /**
     * 发送图片
     * @param file
     * @param sendId
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/sendMsg")
    public String sendMsg(MultipartFile file, int sendId) throws IOException {
        if (sendId != 1 && sendId != 2) {
            return "当前id有误";
        }
        if (file == null) {
            return "图片为空";
        }
        DataEmbedder dataEmbedder = new DataEmbedder();
        synchronized (this) {
            dataEmbedders.add(dataEmbedder);
            embedderCnt++;
        }
        executorService.submit(() -> {
            try {
                EmbedderResponse response = dataEmbedder.Embed(file);
                int imageIndex = response.getImageIndex();
                int id = sendId == 1 ? 2 : 1;
                DecodeResponse decodeResponse = DecodeResponse.builder()
                        .file(DataEmbedder.imageWithInfoPath + imageIndex)
                        .blockOrder(response.getBlockOrder()).build();
                synchronized (this) {
                    receiveIdToFileMap.put(id, decodeResponse);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return "嵌入任务已提交";
    }

    /**
     * 接收图片
     * @param receivedId
     * @return
     */
    @PostMapping(value = "/receiveMsg")
    public DecodeResponse receiveMsg(int receivedId) {
        if (receivedId != 1 && receivedId != 2) {
            throw new RuntimeException("当前id有误");
        }
        return receiveIdToFileMap.get(receivedId);
    }


    @PostMapping(value = "/downloadImage")
    public void downloadImage() {
        //TODO 下载图片
    }

}
