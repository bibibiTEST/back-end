package com.example.rc4_backend.pojo;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Data
@Builder
@ResponseBody
public class ChatMessage {

    private String content;

    private List<Integer> blockOrder;

    public String getContent() {
        return content;
    }
}
