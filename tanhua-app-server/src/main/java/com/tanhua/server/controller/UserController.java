package com.tanhua.server.controller;

import com.tanhua.model.domain.UserInfo;
import com.tanhua.server.Interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("/user")
@RestController
public class UserController {
    @Autowired
    private UserInfoService userInfoService;

    @PostMapping("/loginReginfo")
    public ResponseEntity loginReginfo(@RequestBody UserInfo userInfo, @RequestHeader("Authorization") String token) {

        //2、向userinfo中设置用户id
        userInfo.setId(UserHolder.getUserId());
        userInfoService.save(userInfo);
        return ResponseEntity.ok(null);
    }

    /*@PutMapping
    public ResponseEntity updateUserInfo(@RequestBody UserInfo userInfo,@RequestHeader("Authorization") String token) {
        Claims claims = JwtUtils.getClaims(token);
        Integer id = (Integer) claims.get("id");
        userID = Long.valueOf(id);
        userInfo.setId(UserHolder.getUserId());
        userInfoService.update(userInfo);
        return ResponseEntity.ok(null);
    }*/

    @PostMapping("/loginReginfo/head")
    public ResponseEntity head(MultipartFile head, @RequestHeader("Authorization") String token) throws IOException {
        userInfoService.updateHead(head,UserHolder.getUserId());
        return ResponseEntity.ok(null);
    }


}
