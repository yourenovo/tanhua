package com.tanhua.server.controller;

import com.tanhua.model.dto.RecommendUserDto;
import com.tanhua.model.vo.NearUserVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.TodayBest;
import com.tanhua.server.service.MessagesService;
import com.tanhua.server.service.TanhuaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/tanhua")
@RestController
public class TanhuaController {
    @Autowired
    private TanhuaService tanhuaService;
    @Autowired
    private MessagesService messagesService;

    public ResponseEntity todayBest() {
        TodayBest todayBest = tanhuaService.todayBest();
        return ResponseEntity.ok(todayBest);
    }

    @GetMapping("/recommendation")
    public ResponseEntity recommendation(RecommendUserDto dto) {
        PageResult result = tanhuaService.recommendation(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/personalInfo")
    public ResponseEntity personalInfo(@PathVariable("id") Long userId) {
        TodayBest best = tanhuaService.personalInfo(userId);
        return ResponseEntity.ok(best);
    }

    @GetMapping("/strangerQuestions")
    public ResponseEntity strangerQuestions(Long userId) {
        String questions = tanhuaService.strangerQuestions(userId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/strangerQuestions")
    public ResponseEntity replyQuestions(@RequestBody Map map) {
        Long userId = Long.valueOf(map.get("userId").toString());
        String reply = map.get("reply").toString();
        tanhuaService.replyQuestions(reply, userId);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/contacts")
    public ResponseEntity contacts(@RequestBody Map map) {
        Long friendId = Long.valueOf(map.get("userId").toString());
        messagesService.contacts(friendId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/cards")
    public ResponseEntity queryCardsList() {
        List<TodayBest> list = this.tanhuaService.queryCardsList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("{id}/love")
    public ResponseEntity<Void> likeUser(@PathVariable("id") Long likeUserId) {
        this.tanhuaService.likeUser(likeUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 不喜欢
     */
    @GetMapping("{id}/unlove")
    public ResponseEntity<Void> notLikeUser(@PathVariable("id") Long likeUserId) {
        this.tanhuaService.notLikeUser(likeUserId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/search")
    public ResponseEntity<List<NearUserVo>> queryNearUser(String gender,
                                                          @RequestParam(defaultValue = "2000") String distance) {
        List<NearUserVo> list = this.tanhuaService.queryNearUser(gender, distance);
        return ResponseEntity.ok(list);
    }
}


