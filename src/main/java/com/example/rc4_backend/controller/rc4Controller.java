package com.example.rc4_backend.controller;

import com.example.rc4_backend.pojo.DecodeResponse;
import com.example.rc4_backend.pojo.EmbedderResponse;
import com.example.rc4_backend.tool.DataEmbedder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping("/api/rc4")
public class rc4Controller {
    static HashMap<Integer, DecodeResponse> receiveIdToFileMap = new HashMap<>();
    // List<Integer> idList = new ArrayList<>();
    DataEmbedder dataEmbedder = DataEmbedder.getInstance();

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
        EmbedderResponse response = dataEmbedder.embedder(file);
        int imageIndex = response.getImageIndex();
        // 将嵌入后的图片和密钥矩阵关联到接收人
        int id = sendId == 1 ? 2 : 1;
        DecodeResponse decodeResponse = DecodeResponse.builder()
                .file(DataEmbedder.imageWithInfoPath + imageIndex)
                .setArray(response.getSetArray()).build();
        receiveIdToFileMap.put(id, decodeResponse);
        return "嵌入信息成功";
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
