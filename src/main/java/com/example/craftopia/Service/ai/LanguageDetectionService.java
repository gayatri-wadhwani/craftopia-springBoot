package com.example.craftopia.Service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class LanguageDetectionService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${ai.huggingface.api-url}")
    private String apiUrl;

    @Value("${ai.huggingface.models.language-detection}")
    private String languageDetectionModel;

    private static final Map<String, String> LANGUAGE_CODE_MAP = new HashMap<>();

    static {
        LANGUAGE_CODE_MAP.put("LABEL_0", "ar");
        LANGUAGE_CODE_MAP.put("LABEL_1", "bg");
        LANGUAGE_CODE_MAP.put("LABEL_2", "de");
        LANGUAGE_CODE_MAP.put("LABEL_3", "el");
        LANGUAGE_CODE_MAP.put("LABEL_4", "en");
        LANGUAGE_CODE_MAP.put("LABEL_5", "es");
        LANGUAGE_CODE_MAP.put("LABEL_6", "fr");
        LANGUAGE_CODE_MAP.put("LABEL_7", "hi");
        LANGUAGE_CODE_MAP.put("LABEL_8", "it");
        LANGUAGE_CODE_MAP.put("LABEL_9", "ja");
        LANGUAGE_CODE_MAP.put("LABEL_10", "nl");
        LANGUAGE_CODE_MAP.put("LABEL_11", "pl");
        LANGUAGE_CODE_MAP.put("LABEL_12", "pt");
        LANGUAGE_CODE_MAP.put("LABEL_13", "ru");
        LANGUAGE_CODE_MAP.put("LABEL_14", "sw");
        LANGUAGE_CODE_MAP.put("LABEL_15", "th");
        LANGUAGE_CODE_MAP.put("LABEL_16", "tr");
        LANGUAGE_CODE_MAP.put("LABEL_17", "ur");
        LANGUAGE_CODE_MAP.put("LABEL_18", "vi");
        LANGUAGE_CODE_MAP.put("LABEL_19", "zh");
    }

    public LanguageDetectionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public String detectLanguage(String text) {
//        if (text == null || text.trim().isEmpty()) {
//            System.out.println("Here is the text: " + text);
//            return "en";
//        }

//        if (isLikelyEnglish(text)) {
//            return "en";
//        }

        try {
            String url = apiUrl + "/" + languageDetectionModel;

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("inputs", text.trim());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Language Detection Response: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                response.getBody();
                return parseLanguageDetectionResponse(response.getBody());
            }

            return "en";
        } catch (Exception e) {
            return "en";
        }
    }

    private String parseLanguageDetectionResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.isArray() && jsonNode.size() > 0) {
                JsonNode firstResult = jsonNode.get(0);
                if (firstResult.isArray() && firstResult.size() > 0) {
                    JsonNode topPrediction = firstResult.get(0);
                    if (topPrediction.has("label")) {
                        String label = topPrediction.get("label").asText();
                        return LANGUAGE_CODE_MAP.getOrDefault(label, mapLabelToLanguageCode(label));
                    }
                }
            }

            if (jsonNode.has("label")) {
                String label = jsonNode.get("label").asText();
                return LANGUAGE_CODE_MAP.getOrDefault(label, mapLabelToLanguageCode(label));
            }

            return "en";
        } catch (Exception e) {
            return "en";
        }
    }

    public String detectLanguageWithFallback(String text) {
        try {
            return detectLanguage(text);
        } catch (Exception e) {
            return isLikelyEnglish(text) ? "en" : "unknown";
        }
    }

    private boolean isLikelyEnglish(String text) {
        if (text == null || text.trim().isEmpty()) {
            return true;
        }

        String lowerText = text.toLowerCase();
        String[] commonWords = {"the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by"};

        int count = 0;
        for (String word : commonWords) {
            if (lowerText.contains(" " + word + " ") || lowerText.startsWith(word + " ") || lowerText.endsWith(" " + word)) {
                count++;
            }
        }

        return count >= 2;
    }

    private String mapLabelToLanguageCode(String label) {
        if (label == null) return "en";

        String lowerLabel = label.toLowerCase();

        if (lowerLabel.contains("english")) return "en";
        if (lowerLabel.contains("spanish")) return "es";
        if (lowerLabel.contains("french")) return "fr";
        if (lowerLabel.contains("german")) return "de";
        if (lowerLabel.contains("italian")) return "it";
        if (lowerLabel.contains("portuguese")) return "pt";
        if (lowerLabel.contains("russian")) return "ru";
        if (lowerLabel.contains("chinese")) return "zh";
        if (lowerLabel.contains("japanese")) return "ja";
        if (lowerLabel.contains("korean")) return "ko";
        if (lowerLabel.contains("arabic")) return "ar";
        if (lowerLabel.contains("hindi")) return "hi";
        if (lowerLabel.contains("marathi")) return "mr";
        if (lowerLabel.contains("bengali")) return "bn";
        if (lowerLabel.contains("tamil")) return "ta";
        if (lowerLabel.contains("telugu")) return "te";
        if (lowerLabel.contains("gujarati")) return "gu";
        if (lowerLabel.contains("kannada")) return "kn";
        if (lowerLabel.contains("malayalam")) return "ml";
        if (lowerLabel.contains("punjabi")) return "pa";
        if (lowerLabel.contains("urdu")) return "ur";

        if (label.length() >= 2) {
            return label.substring(0, 2).toLowerCase();
        }

        return "en";
    }
}
