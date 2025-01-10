package com.example.rc4_backend.pojo;

import lombok.Data;

import java.util.List;

@Data
public class EmbedderResponse {

    private List<Integer> blockOrder;
    private String embedderdFilePath;
}
