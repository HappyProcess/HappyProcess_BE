package com.haapyProcess.global.gemini;

import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Generative Language(Gemini) REST API 호출 클라이언트.
 * SDK 의존성 없이 RestClient로 generateContent 엔드포인트를 직접 호출한다.
 */
@Slf4j
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String apiUrl;

    public GeminiClient(RestClient restClient,
                        @Value("${gemini.api-key:}") String apiKey,
                        @Value("${gemini.model:gemini-3.1-flash-lite}") String model,
                        @Value("${gemini.api-url:https://generativelanguage.googleapis.com/v1beta}") String apiUrl) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.model = model;
        this.apiUrl = apiUrl;
    }

    /**
     * 주어진 프롬프트로 텍스트를 생성한다.
     * @return 생성된 텍스트
     */
    public String generate(String prompt) {
        return doGenerate(prompt, false);
    }

    /**
     * JSON 형식으로 응답을 강제(responseMimeType=application/json)하여 생성한다.
     * @return JSON 문자열
     */
    public String generateJson(String prompt) {
        return doGenerate(prompt, true);
    }

    private String doGenerate(String prompt, boolean jsonMode) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Gemini API 키가 설정되지 않았습니다. (gemini.api-key / GEMINI_API_KEY)");
            throw new CustomException(ErrorCode.REPORT_GENERATION_FAILED);
        }

        String url = apiUrl + "/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        if (jsonMode) {
            body.put("generationConfig", Map.of("responseMimeType", "application/json"));
        }

        try {
            GeminiResponse response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(GeminiResponse.class);

            return extractText(response);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.REPORT_GENERATION_FAILED);
        }
    }

    private String extractText(GeminiResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            log.error("Gemini 응답에 candidates가 없습니다.");
            throw new CustomException(ErrorCode.REPORT_GENERATION_FAILED);
        }
        Content content = response.candidates().get(0).content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            log.error("Gemini 응답에 content/parts가 없습니다.");
            throw new CustomException(ErrorCode.REPORT_GENERATION_FAILED);
        }
        String text = content.parts().get(0).text();
        if (text == null || text.isBlank()) {
            throw new CustomException(ErrorCode.REPORT_GENERATION_FAILED);
        }
        return text.trim();
    }

    // --- Gemini generateContent 응답 매핑 (필요한 필드만) ---
    record GeminiResponse(List<Candidate> candidates) {}
    record Candidate(Content content) {}
    record Content(List<Part> parts) {}
    record Part(String text) {}
}
