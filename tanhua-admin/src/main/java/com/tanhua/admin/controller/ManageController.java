package com.tanhua.admin.controller;

import com.tanhua.admin.service.ManagerService;
import com.tanhua.model.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/manage")
public class ManageController {
    @Autowired
    private ManagerService managerService;

    @GetMapping("/users")
    public ResponseEntity users(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize) {
        return managerService.findAllUsers(page, pagesize);

    }

    @GetMapping("/users/{userId}")
    public ResponseEntity findById(@PathVariable("userId") Long userId) {
        return managerService.findById(userId);
    }

    @GetMapping("/videos")
    public ResponseEntity videos(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer pagesize,
                                 Long uid ) {
        PageResult result = managerService.findAllVideos(page,pagesize,uid);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/messages")
    public ResponseEntity messages(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   Long uid,Integer state ) {
        PageResult result = managerService.findAllMovements(page,pagesize,uid,state);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/users/freeze")
    public ResponseEntity freeze(@RequestBody Map params) {
        Map map =  managerService.userFreeze(params);
        return ResponseEntity.ok(map);
    }

    @PostMapping("/users/unfreeze")
    public ResponseEntity unfreeze(@RequestBody  Map params) {
        Map map =  managerService.userUnfreeze(params);
        return ResponseEntity.ok(map);
    }

}
