package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.SettingsVo;
import com.tanhua.server.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class SettingController {
    @Autowired
    private SettingService service;

    @GetMapping("/settings")
    public ResponseEntity settings() {
        SettingsVo settingsVo = service.settings();
        return ResponseEntity.ok(settingsVo);
    }

    @PostMapping("/questions")
    public ResponseEntity questions(@RequestBody Map map){
        String context = (String) map.get("context");
        service.saveQuestion(context);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/notifications/setting")
    public ResponseEntity notifications(@RequestBody Map map) {
        //获取参数
        service.saveSettings(map);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/blacklist")
    public ResponseEntity blacklist(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        //1、调用service查询
        PageResult pr = service.blacklist(page,size);
        //2、构造返回
        return ResponseEntity.ok(pr);
    }

    @DeleteMapping("/blacklist/{uid}")
    public ResponseEntity deleteBlackList(@PathVariable("uid") Long blackUserId) {
        service.deleteBlackList(blackUserId);
        return ResponseEntity.ok(null);
    }

}
