package com.example.rc4_backend;

import com.example.rc4_backend.pojo.EmbedderResponse;
import com.example.rc4_backend.tool.DataEmbedder;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TestMain {


    public static void main(String[] args) throws IOException {
        String dataHidingKey = "key";
        DataEmbedder dataEmbedder = DataEmbedder.getInstance();
        String filePath = "encrypted_image2.png";
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",              // 文件名
                file.getName(),      // 原始文件名
                "image/jpeg",        // 文件类型（根据需要调整）
                fileInputStream      // 文件输入流
        );

//        dataEmbedder.test( "encrypted_image2.png",5,5);
        EmbedderResponse response = dataEmbedder.Embed(multipartFile);
    }

}
