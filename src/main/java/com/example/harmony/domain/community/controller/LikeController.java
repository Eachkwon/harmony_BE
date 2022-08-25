package com.example.harmony.domain.community.controller;

import com.example.harmony.domain.community.service.LikeService;
import com.example.harmony.global.common.SuccessResponse;
import com.example.harmony.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
public class LikeController {

    private final LikeService likeService;

    // 게시글 좋아요
    @PutMapping("/api/posts/{postId}/likes")
    public ResponseEntity<?> doLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String,Boolean> map ) {
        String msg = "좋아요를 눌렀습니다.";
        likeService.doLike(postId, userDetails.getUser(), map.get("like"));
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, msg));
    }

    // 게시글 좋아요 취소
    @DeleteMapping("/api/posts/{postId}/likes")
    public ResponseEntity<?> undoLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails ) {
        String msg = "좋아요를 취소하였습니다.";
        likeService.undoLike(postId, userDetails.getUser());
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, msg));
    }
}
