package com.aivle.bookapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommentUpdateRequest(

        @Schema(description = "댓글 내용")
        String text,

        @Schema(description = "비밀번호")
        String password
) {}