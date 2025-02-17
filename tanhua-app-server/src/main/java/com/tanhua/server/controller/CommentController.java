package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.server.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    public ResponseEntity saveComments(@RequestBody Map map) {
        String movementId = (String) map.get("movementId");
        String comment = (String) map.get("comment");
        commentService.saveComments(movementId, comment);
        return ResponseEntity.ok(null);
    }

    @GetMapping
    public ResponseEntity findComments(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pagesize,
                                       String movementId) {
        PageResult pr = commentService.findComments(movementId, page, pagesize);
        return ResponseEntity.ok(pr);
    }

    @GetMapping("/{id}/like")
    public ResponseEntity like(@PathVariable("id") String movementId) {
        Integer likeComment = commentService.likeComment(movementId);
        return ResponseEntity.ok(likeComment);
    }

    @GetMapping("/{id}/dislike")
    public ResponseEntity dislike(@PathVariable("id") String movementId) {
        Object commentsService;
        Integer likeCount = commentService.dislikeComment(movementId);
        return ResponseEntity.ok(likeCount);
    }

    @GetMapping("/{id}/love")
    public ResponseEntity love(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.loveComment(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 取消喜欢
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity unlove(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.disloveComment(movementId);
        return ResponseEntity.ok(likeCount);
    }
}
