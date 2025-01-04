package com.example.rc4_backend.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DecodeResponse {

    private String file;

    private int[] blockOrder;
//    private int[][] setArray;
}
