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
public class TranslationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.huggingface.api-url}")
    private String apiUrl;

    @Value("${ai.huggingface.models.translation}")
    private String translationModel;

    @Value("${ai.huggingface.token}")
    private String huggingfaceToken;

    public TranslationService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String translateToEnglish(String text, String sourceLanguage) {
        System.out.println("Translating text to English: " + text + " from language: " + sourceLanguage);
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        if ("en".equalsIgnoreCase(sourceLanguage)) {
            return text;
        }

        try {
            String url = apiUrl + "/" + translationModel;
            String translationPrompt = prepareTranslationPrompt(text, sourceLanguage, "en");

            Map<String, String> body = new HashMap<>();
            body.put("inputs", translationPrompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(huggingfaceToken);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            System.out.println(response);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseTranslationResponse(response.getBody());
            }

        } catch (Exception ignored) {}

        return text;
    }

    public String translateFromEnglish(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        if ("en".equalsIgnoreCase(targetLanguage)) {
            return text;
        }

        try {
            String url = apiUrl + "/" + translationModel;
            String translationPrompt = prepareTranslationPrompt(text, "en", targetLanguage);

            Map<String, String> body = new HashMap<>();
            body.put("inputs", translationPrompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(huggingfaceToken);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseTranslationResponse(response.getBody());
            }

        } catch (Exception ignored) {}

        return text;
    }

    public String translateToEnglishWithFallback(String text, String sourceLanguage) {
        try {
            return translateToEnglish(text, sourceLanguage);
        } catch (Exception e) {
            return text;
        }
    }

    public String translateFromEnglishWithFallback(String text, String targetLanguage) {
        try {
            return translateFromEnglish(text, targetLanguage);
        } catch (Exception e) {
            return text;
        }
    }

    private String prepareTranslationPrompt(String text, String sourceLanguage, String targetLanguage) {
        String sourceLang = getLanguageName(sourceLanguage);
        String targetLang = getLanguageName(targetLanguage);
        return String.format("translate %s to %s: %s", sourceLang, targetLang, text);
    }

    private String getLanguageName(String languageCode) {
        if (languageCode == null) return "English";
        switch (languageCode.toLowerCase()) {
            case "en": return "English";
            case "es": return "Spanish";
            case "fr": return "French";
            case "de": return "German";
            case "it": return "Italian";
            case "pt": return "Portuguese";
            case "ru": return "Russian";
            case "zh": return "Chinese";
            case "ja": return "Japanese";
            case "ko": return "Korean";
            case "ar": return "Arabic";
            case "hi": return "Hindi";
            case "mr": return "Marathi";
            case "bn": return "Bengali";
            case "ta": return "Tamil";
            case "te": return "Telugu";
            case "gu": return "Gujarati";
            case "kn": return "Kannada";
            case "ml": return "Malayalam";
            case "pa": return "Punjabi";
            case "ur": return "Urdu";
            case "nl": return "Dutch";
            case "pl": return "Polish";
            case "tr": return "Turkish";
            case "th": return "Thai";
            case "vi": return "Vietnamese";
            case "sw": return "Swahili";
            case "bg": return "Bulgarian";
            case "el": return "Greek";
            default: return "English";
        }
    }

    private String parseTranslationResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.isArray() && jsonNode.size() > 0) {
                JsonNode firstResult = jsonNode.get(0);
                if (firstResult.has("generated_text")) {
                    return cleanTranslatedText(firstResult.get("generated_text").asText());
                }
            }

            if (jsonNode.has("generated_text")) {
                return cleanTranslatedText(jsonNode.get("generated_text").asText());
            }

        } catch (Exception ignored) {}

        return "Translation unavailable";
    }

    private String cleanTranslatedText(String translatedText) {
        if (translatedText == null) return "";

        String cleaned = translatedText.trim();
        String[] prefixesToRemove = {
                "translate ", "translation:", "translated:", "result:", "output:"
        };

        for (String prefix : prefixesToRemove) {
            if (cleaned.toLowerCase().startsWith(prefix)) {
                cleaned = cleaned.substring(prefix.length()).trim();
            }
        }

        return cleaned;
    }
}
