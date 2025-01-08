package com.example.rc4_backend.tool;

import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolTool {
    public static final ThreadPoolExecutor embedderThreadPool
            = new ThreadPoolExecutor(4, 8, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>(10));
}
