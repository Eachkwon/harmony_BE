package com.example.harmony.domain.gallery.controller;

import com.example.harmony.domain.gallery.dto.GalleryCommentRequest;
import com.example.harmony.domain.gallery.service.GalleryCommentService;
import com.example.harmony.global.common.SuccessResponse;
import com.example.harmony.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class GalleryCommentController {

    private final GalleryCommentService galleryCommentService;

    @PostMapping("/api/galleries/{galleryId}/comments")
    ResponseEntity<SuccessResponse> postComment(
            @PathVariable Long galleryId,
            @RequestBody GalleryCommentRequest galleryCommentRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        galleryCommentService.writeGalleryComment(galleryId, galleryCommentRequest, userDetails.getUser());
        return new ResponseEntity<>(new SuccessResponse(HttpStatus.OK, "갤러리 댓글 작성 성공"), HttpStatus.OK);
    }
}
