package com.example.rc4_backend.pojo;

import lombok.Data;

@Data
public class EmbedderResponse {

    private int[] blockOrder;
    private String embedderdFilePath;
}
