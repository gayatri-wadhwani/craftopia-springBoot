package com.example.craftopia.Service.ai;

import com.example.craftopia.Entity.ArtStyle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SummaryGenerationService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${ai.huggingface.api-url}")
    private String apiUrl;

    @Value("${ai.huggingface.models.summarization}")
    private String summarizationModel;

    public SummaryGenerationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public String generateTitle(String content, ArtStyle artStyle, List<String> tags) {
        if (content == null || content.trim().isEmpty()) {
            return generateFallbackTitle(artStyle, tags);
        }

        try {
            String prompt = prepareTitleGenerationPrompt(content, artStyle, tags);
            String response = callSummarizationAPI(prompt);
            return parseTitleResponse(response, artStyle, tags);
        } catch (Exception e) {
            return generateFallbackTitle(artStyle, tags);
        }
    }

    public String generateDescription(String content, ArtStyle artStyle, List<String> tags, String imageCaption) {
        if (content == null || content.trim().isEmpty()) {
            return generateFallbackDescription(artStyle, tags, imageCaption);
        }

        try {
            String prompt = prepareDescriptionGenerationPrompt(content, artStyle, tags, imageCaption);
            String response = callSummarizationAPI(prompt);
            return parseDescriptionResponse(response, artStyle, tags, imageCaption);
        } catch (Exception e) {
            return generateFallbackDescription(artStyle, tags, imageCaption);
        }
    }

    private String callSummarizationAPI(String prompt) {
        String url = apiUrl + "/" + summarizationModel;
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("inputs", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }

    private String prepareTitleGenerationPrompt(String content, ArtStyle artStyle, List<String> tags) {
        StringBuilder prompt = new StringBuilder("Create a short, catchy title for this product: ");
        prompt.append(content, 0, Math.min(200, content.length()));
        if (artStyle != null && artStyle != ArtStyle.UNKNOWN)
            prompt.append(" Art style: ").append(artStyle.getDisplayName());
        if (tags != null && !tags.isEmpty())
            prompt.append(" Keywords: ").append(String.join(", ", tags.subList(0, Math.min(5, tags.size()))));
        return prompt.toString();
    }

    private String prepareDescriptionGenerationPrompt(String content, ArtStyle artStyle, List<String> tags, String imageCaption) {
        StringBuilder prompt = new StringBuilder("Write a detailed product description based on: ");
        prompt.append(content, 0, Math.min(300, content.length()));
        if (imageCaption != null && !imageCaption.isBlank())
            prompt.append(" Image shows: ").append(imageCaption);
        if (artStyle != null && artStyle != ArtStyle.UNKNOWN)
            prompt.append(" This is ").append(artStyle.getDisplayName()).append(" art.");
        if (tags != null && !tags.isEmpty())
            prompt.append(" Features: ").append(String.join(", ", tags.subList(0, Math.min(8, tags.size()))));
        return prompt.toString();
    }

    private String parseTitleResponse(String response, ArtStyle artStyle, List<String> tags) {
        try {
            JsonNode json = objectMapper.readTree(response);
            String text = extractTextFromResponse(json);
            return text != null ? cleanAndFormatTitle(text, artStyle) : generateFallbackTitle(artStyle, tags);
        } catch (Exception e) {
            return generateFallbackTitle(artStyle, tags);
        }
    }

    private String parseDescriptionResponse(String response, ArtStyle artStyle, List<String> tags, String imageCaption) {
        try {
            JsonNode json = objectMapper.readTree(response);
            String text = extractTextFromResponse(json);
            return text != null ? cleanAndFormatDescription(text, artStyle) : generateFallbackDescription(artStyle, tags, imageCaption);
        } catch (Exception e) {
            return generateFallbackDescription(artStyle, tags, imageCaption);
        }
    }

    private String extractTextFromResponse(JsonNode json) {
        if (json.isArray() && json.size() > 0) {
            JsonNode first = json.get(0);
            if (first.has("summary_text")) return first.get("summary_text").asText();
            if (first.has("generated_text")) return first.get("generated_text").asText();
        }
        if (json.has("summary_text")) return json.get("summary_text").asText();
        if (json.has("generated_text")) return json.get("generated_text").asText();
        return null;
    }

    private String cleanAndFormatTitle(String title, ArtStyle artStyle) {
        if (title == null) return generateFallbackTitle(artStyle, null);
        String cleaned = title.trim();

        for (String prefix : new String[]{"title:", "product:", "name:", "summary:", "create a title:", "generate title:"}) {
            if (cleaned.toLowerCase().startsWith(prefix)) {
                cleaned = cleaned.substring(prefix.length()).trim();
            }
        }

        if (!cleaned.isEmpty()) {
            cleaned = cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
        }

        if (cleaned.length() > 80) {
            cleaned = cleaned.substring(0, 77) + "...";
        }

        return cleaned.isEmpty() ? generateFallbackTitle(artStyle, null) : cleaned;
    }

    private String cleanAndFormatDescription(String description, ArtStyle artStyle) {
        if (description == null) return generateFallbackDescription(artStyle, null, null);
        String cleaned = description.trim();

        for (String prefix : new String[]{"description:", "product description:", "summary:", "write a description:", "generate description:"}) {
            if (cleaned.toLowerCase().startsWith(prefix)) {
                cleaned = cleaned.substring(prefix.length()).trim();
            }
        }

        if (!cleaned.isEmpty()) {
            cleaned = cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
        }

        if (!cleaned.isEmpty() && !cleaned.endsWith(".") && !cleaned.endsWith("!") && !cleaned.endsWith("?")) {
            cleaned += ".";
        }

        return cleaned.isEmpty() ? generateFallbackDescription(artStyle, null, null) : cleaned;
    }

    private String generateFallbackTitle(ArtStyle artStyle, List<String> tags) {
        StringBuilder title = new StringBuilder();

        if (artStyle != null && artStyle != ArtStyle.UNKNOWN)
            title.append(artStyle.getDisplayName()).append(" ");

        title.append("Handcrafted ");

        if (tags != null && !tags.isEmpty()) {
            String tag = tags.get(0);
            if (!tag.toLowerCase().contains("handmade") && !tag.toLowerCase().contains("craft")) {
                title.append(tag.substring(0, 1).toUpperCase()).append(tag.substring(1)).append(" ");
            }
        }

        title.append("Artwork");
        return title.toString();
    }

    private String generateFallbackDescription(ArtStyle artStyle, List<String> tags, String imageCaption) {
        StringBuilder desc = new StringBuilder("This beautiful ");

        if (artStyle != null && artStyle != ArtStyle.UNKNOWN)
            desc.append(artStyle.getDisplayName().toLowerCase()).append(" ");

        desc.append("artwork is handcrafted with attention to detail. ");

        if (imageCaption != null && !imageCaption.trim().isEmpty()) {
            desc.append("The piece features ").append(imageCaption.toLowerCase()).append(". ");
        }

        if (tags != null && !tags.isEmpty()) {
            desc.append("Perfect for those who appreciate ");
            desc.append(String.join(", ", tags.subList(0, Math.min(3, tags.size())))).append(". ");
        }

        desc.append("A unique addition to any collection or home decor.");
        return desc.toString();
    }

    public String generateTitleWithFallback(String content, ArtStyle artStyle, List<String> tags) {
        try {
            return generateTitle(content, artStyle, tags);
        } catch (Exception e) {
            return generateFallbackTitle(artStyle, tags);
        }
    }

    public String generateDescriptionWithFallback(String content, ArtStyle artStyle, List<String> tags, String imageCaption) {
        try {
            return generateDescription(content, artStyle, tags, imageCaption);
        } catch (Exception e) {
            return generateFallbackDescription(artStyle, tags, imageCaption);
        }
    }
}
