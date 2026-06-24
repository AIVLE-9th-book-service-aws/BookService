package com.aivle.bookapp.exception;

import lombok.Getter;

@Getter
public class OpenAiException extends RuntimeException {
    private final int statusCode;

    public OpenAiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public static OpenAiException from(int statusCode, String responseBody) {
        if (statusCode == 401) return new OpenAiException(401, "API Key가 올바르지 않습니다.");
        if (statusCode == 403) return new OpenAiException(403, "접근 권한 없음. 요금제를 변경해주세요.");
        if (statusCode == 404) return new OpenAiException(404, "API 경로 오류.");
        if (statusCode == 408) return timeout();
        if (statusCode == 429) return new OpenAiException(429, "요청 한도 초과. 잠시 후 다시 시도해주세요.");
        if (statusCode == 500) return new OpenAiException(500, "OpenAI 서버 오류. 잠시 후 다시 시도해주세요.");
        if (statusCode == 503) return new OpenAiException(503, "OpenAI 서버 과부하. 잠시 후 다시 시도해주세요.");
        return new OpenAiException(statusCode, "OpenAI 오류가 발생했습니다. (code: " + statusCode + ")");
    }

    public static OpenAiException timeout() {
        return new OpenAiException(408, "OpenAI 응답 시간 초과. 잠시 후 다시 시도해주세요.");
    }

    public static OpenAiException interrupted() {
        return new OpenAiException(500, "요청이 중단되었습니다.");
    }

    public static OpenAiException callFailed(Exception e) {
        String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        return new OpenAiException(502, "OpenAI 연결 실패: " + detail);
    }

    public static OpenAiException missingData() {
        return new OpenAiException(502, "OpenAI 응답 형식 오류: data 필드 없음");
    }

    public static OpenAiException missingImage() {
        return new OpenAiException(502, "OpenAI 응답 형식 오류: 이미지 데이터 없음");
    }
}
