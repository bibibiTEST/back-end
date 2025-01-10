package com.example.rc4_backend.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DecodeResponse {

    private String file;

    private List<Integer> blockOrder;
//    private int[][] setArray;
}
