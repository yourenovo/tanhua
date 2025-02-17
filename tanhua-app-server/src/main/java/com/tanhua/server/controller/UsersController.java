package com.tanhua.server.controller;

import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.Interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UserInfoService userInfoService;

    @GetMapping
    public ResponseEntity user(@RequestHeader("Authorization") String token, Long useId) {

        if (useId == null) {
            useId = UserHolder.getUserId();
        }
        UserInfoVo userInfoVo = userInfoService.findById(useId);
        return ResponseEntity.ok(userInfoVo);
    }

    @PutMapping
    public ResponseEntity updateUserInfo(@RequestBody UserInfo userInfo, @RequestHeader("Authorization") String token) {
        userInfo.setId(UserHolder.getUserId());
        userInfoService.update(userInfo);
        return ResponseEntity.ok(null);
    }
}
