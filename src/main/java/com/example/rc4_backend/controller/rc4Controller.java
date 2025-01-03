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

    DataEmbedder dataEmbedder = DataEmbedder.getInstance();

    @PostMapping(value = "/sendMsg")
    public String sendMsg(MultipartFile file, int sendId) throws IOException {
        EmbedderResponse response = dataEmbedder.embedder(file);
        int imageIndex = response.getImageIndex();
        // 将嵌入后的图片和密钥矩阵关联到接收人
        int id = sendId == 1 ? 2 : 1;
        DecodeResponse decodeResponse = new DecodeResponse();
        decodeResponse.setFile(DataEmbedder.imageWithInfoPath + imageIndex);
        decodeResponse.setSetArray(response.getSetArray());
        receiveIdToFileMap.put(id, decodeResponse);
        return "嵌入信息成功";
    }

    @PostMapping(value = "/receiveMsg")
    public DecodeResponse receiveMsg(int receivedId) {
        return receiveIdToFileMap.get(receivedId);
    }

}
