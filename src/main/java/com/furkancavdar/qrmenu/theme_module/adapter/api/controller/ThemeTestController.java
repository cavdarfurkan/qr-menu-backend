package com.furkancavdar.qrmenu.theme_module.adapter.api.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/theme/test")
public class ThemeTestController {

    private final StringRedisTemplate stringRedisTemplate;

    public ThemeTestController(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @GetMapping("/build")
    public void test() {
//        stringRedisTemplate.executePipelined((RedisCallback<String>) connection -> {
//        });
        String job = "{\"id\":\"aaa\", \"time\": \"" + System.currentTimeMillis() + "\"}";
        stringRedisTemplate.opsForList().leftPush("queue:build:main", job);
    }
}
