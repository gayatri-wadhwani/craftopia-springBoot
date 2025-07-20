package com.example.craftopia.Service;

import com.example.craftopia.DTO.AIGeneratedProductDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiProductEnrichmentService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIGeneratedProductDetails enrichProduct(String imageBase64, String userCaption) {
        try {
            String prompt = createEnrichmentPrompt(userCaption);
            String response = callGeminiAPI(prompt, imageBase64);
            return parseProductDetails(response);
        } catch (Exception e) {
            System.err.println("Error enriching product: " + e.getMessage());
            e.printStackTrace();
            return createFallbackProductDetails(userCaption);
        }
    }

    private String createEnrichmentPrompt(String userCaption) {
        return String.format("""
                You are helping a rural artisan sell their handmade product online.
                Analyze the image and caption provided, then return ONLY a valid JSON response with these fields:

                {
                  "detectedLanguage": "language name",
                  "englishTitle": "SEO-friendly title (max 60 chars)",
                  "englishDescription": "detailed description (50-100 words)",
                  "tags": ["tag1", "tag2", "tag3", "tag4", "tag5"],
                  "category": "main category",
                  "subCategory": "subcategory",
                  "suggestedPrice": "₹500-800",
                  "materials": ["material1", "material2"],
                  "colors": ["color1", "color2"],
                  "size": "approx size",
                  "occasion": "best suited occasion"
                }

                Guidelines:
                - Title must be attractive and SEO-optimized
                - Highlight uniqueness, cultural value, craftsmanship
                - Price should be fair for Indian handmade market
                - Translate if user caption is in regional language

                User's Caption: "%s"
                """, userCaption != null ? userCaption : "No caption provided");
    }

    private String callGeminiAPI(String prompt, String imageBase64) throws Exception {
        String url = apiUrl + "?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt),
                                Map.of("inline_data", Map.of(
                                        "mime_type", "image/jpeg",
                                        "data", imageBase64
                                ))
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.4,
                        "topK", 32,
                        "topP", 1,
                        "maxOutputTokens", 1000
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Gemini API call failed: " + response.getStatusCode());
        }

        return extractTextFromResponse(response.getBody());
    }

    private String extractTextFromResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        return root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
    }

    private AIGeneratedProductDetails parseProductDetails(String jsonResponse) throws Exception {
        String cleanJson = jsonResponse
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        try {
            return objectMapper.readValue(cleanJson, AIGeneratedProductDetails.class);
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini JSON: " + cleanJson);
            e.printStackTrace();
            return createFallbackProductDetails(cleanJson);
        }
    }

    private AIGeneratedProductDetails createFallbackProductDetails(String input) {
        return AIGeneratedProductDetails.builder()
                .detectedLanguage("Unknown")
                .englishTitle("Handmade Craft Item")
                .englishDescription("A beautiful handmade product crafted by a skilled artisan.")
                .tags(List.of("handmade", "craft", "artisan", "traditional", "unique"))
                .category("Handicrafts")
                .subCategory("Miscellaneous")
                .suggestedPrice("₹300-600")
                .materials(List.of("Mixed materials"))
                .colors(List.of("Multicolor"))
                .size("Medium")
                .occasion("Gifting, Home decor")
                .build();
    }

    // Optional: for just language detection if needed separately
    public String detectLanguage(String text) {
        try {
            String prompt = "Detect the language of this text and respond with ONLY the language name: \"" + text + "\"";

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            return callGeminiTextAPI(requestBody).trim();
        } catch (Exception e) {
            System.err.println("Language detection failed: " + e.getMessage());
            return "English"; // fallback
        }
    }

    private String callGeminiTextAPI(Map<String, Object> requestBody) throws Exception {
        String url = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return extractTextFromResponse(response.getBody());
    }

    public String translateText(String originalText) {
        try {
            String prompt = String.format("""
            Translate the following text to English. Respond with ONLY the translated text, no JSON or extra words.

            Original: "%s"
        """, originalText);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.2,
                            "topK", 32,
                            "topP", 1,
                            "maxOutputTokens", 200
                    )
            );

            return callGeminiTextAPI(requestBody).trim();

        } catch (Exception e) {
            System.err.println("Translation failed: " + e.getMessage());
            return originalText; // fallback to original
        }
    }

}
