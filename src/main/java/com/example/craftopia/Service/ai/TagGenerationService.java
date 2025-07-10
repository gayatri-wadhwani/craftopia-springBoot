package com.example.craftopia.Service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagGenerationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.huggingface.api-url}")
    private String apiUrl;

    @Value("${ai.huggingface.models.tag-generation}")
    private String tagGenerationModel;

    // Predefined tags for Indian art and crafts
    private static final Set<String> INDIAN_ART_TAGS = Set.of(
            "warli", "gond", "madhubani", "pattachitra", "kalamkari", "tanjore", "pichwai",
            "mata ni pachedi", "kalighat", "bhil", "folk art", "traditional", "handmade",
            "handcrafted", "artisan", "cultural", "heritage", "ethnic", "tribal", "village art",
            "indian art", "desi", "indigenous", "authentic", "original", "unique", "decorative",
            "wall art", "home decor", "festival", "diwali", "holi", "navratri", "spiritual",
            "religious", "mythological", "nature", "peacock", "elephant", "lotus", "mandala",
            "geometric", "floral", "abstract", "colorful", "vibrant", "earthy", "natural",
            "eco-friendly", "sustainable", "khadi", "cotton", "silk", "canvas", "paper",
            "gift", "souvenir", "collectible", "artwork", "painting", "drawing", "craft"
    );

    public TagGenerationService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public List<String> generateTags(String content) {
        if (content == null || content.trim().isEmpty()) {
            return getDefaultTags();
        }

        try {
            String url = apiUrl + "/" + tagGenerationModel;
            String prompt = "generate tags: " + content.trim();

            Map<String, String> body = new HashMap<>();
            body.put("inputs", prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseTagGenerationResponse(response.getBody(), content);
            } else {
                return generateFallbackTags(content);
            }

        } catch (Exception e) {
            return generateFallbackTags(content);
        }
    }

    private List<String> parseTagGenerationResponse(String response, String originalContent) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);

            String generatedText = null;

            if (jsonNode.isArray() && jsonNode.size() > 0) {
                JsonNode firstResult = jsonNode.get(0);
                if (firstResult.has("generated_text")) {
                    generatedText = firstResult.get("generated_text").asText();
                }
            }

            if (jsonNode.has("generated_text")) {
                generatedText = jsonNode.get("generated_text").asText();
            }

            if (jsonNode.has("error")) {
                return generateFallbackTags(originalContent);
            }

            if (generatedText != null) {
                List<String> tags = extractTagsFromText(generatedText);
                return enhanceWithContextualTags(tags, originalContent);
            }

            return generateFallbackTags(originalContent);

        } catch (Exception e) {
            return generateFallbackTags(originalContent);
        }
    }

    private List<String> extractTagsFromText(String generatedText) {
        if (generatedText == null || generatedText.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String cleaned = generatedText.toLowerCase().trim();
        String[] prefixes = {"tags:", "generate tags:", "generated tags:", "keywords:", "labels:"};

        for (String prefix : prefixes) {
            if (cleaned.startsWith(prefix)) {
                cleaned = cleaned.substring(prefix.length()).trim();
            }
        }

        String[] delimiters = {",", ";", "\\|", "\\n", "\\t"};
        List<String> tags = new ArrayList<>();

        for (String delimiter : delimiters) {
            String[] parts = cleaned.split(delimiter);
            for (String part : parts) {
                String tag = part.trim();
                if (tag.length() > 1 && tag.length() < 30) {
                    tags.add(tag);
                }
            }
            if (tags.size() > 3) break;
        }

        if (tags.isEmpty()) {
            String[] words = cleaned.split("\\s+");
            for (String word : words) {
                String tag = word.trim().replaceAll("[^a-zA-Z0-9\\s]", "");
                if (tag.length() > 2 && tag.length() < 20) {
                    tags.add(tag);
                }
            }
        }

        return tags.stream().distinct().limit(10).collect(Collectors.toList());
    }

    private List<String> enhanceWithContextualTags(List<String> generatedTags, String content) {
        Set<String> allTags = new HashSet<>(generatedTags);
        String lowerContent = content.toLowerCase();

        for (String artTag : INDIAN_ART_TAGS) {
            if (lowerContent.contains(artTag)) {
                allTags.add(artTag);
            }
        }

        if (lowerContent.contains("hand") || lowerContent.contains("craft")) {
            allTags.add("handcrafted");
        }
        if (lowerContent.contains("traditional") || lowerContent.contains("folk")) {
            allTags.add("traditional");
        }
        if (lowerContent.contains("art") || lowerContent.contains("paint")) {
            allTags.add("artwork");
        }
        if (lowerContent.contains("decor") || lowerContent.contains("decoration")) {
            allTags.add("home decor");
        }
        if (lowerContent.contains("gift") || lowerContent.contains("present")) {
            allTags.add("gift");
        }
        if (lowerContent.contains("color") || lowerContent.contains("vibrant")) {
            allTags.add("colorful");
        }
        if (lowerContent.contains("unique") || lowerContent.contains("special")) {
            allTags.add("unique");
        }

        return allTags.stream()
                .filter(tag -> tag.length() > 1 && tag.length() < 25)
                .distinct()
                .limit(15)
                .collect(Collectors.toList());
    }

    private List<String> generateFallbackTags(String content) {
        List<String> fallbackTags = new ArrayList<>();
        String lowerContent = content.toLowerCase();

        if (lowerContent.contains("warli")) fallbackTags.add("warli");
        if (lowerContent.contains("gond")) fallbackTags.add("gond");
        if (lowerContent.contains("madhubani")) fallbackTags.add("madhubani");
        if (lowerContent.contains("art")) fallbackTags.add("artwork");
        if (lowerContent.contains("hand")) fallbackTags.add("handmade");
        if (lowerContent.contains("traditional")) fallbackTags.add("traditional");
        if (lowerContent.contains("folk")) fallbackTags.add("folk art");
        if (lowerContent.contains("paint")) fallbackTags.add("painting");
        if (lowerContent.contains("craft")) fallbackTags.add("handcrafted");
        if (lowerContent.contains("decor")) fallbackTags.add("home decor");

        if (fallbackTags.isEmpty()) {
            fallbackTags.addAll(getDefaultTags());
        }

        return fallbackTags;
    }

    private List<String> getDefaultTags() {
        return Arrays.asList("handmade", "artwork", "traditional", "unique", "decorative");
    }

    public List<String> generateTagsWithFallback(String content) {
        try {
            return generateTags(content);
        } catch (Exception e) {
            return generateFallbackTags(content);
        }
    }
}
