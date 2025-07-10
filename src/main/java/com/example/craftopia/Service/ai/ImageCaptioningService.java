package com.example.craftopia.Service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageCaptioningService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${ai.huggingface.api-url}")
    private String apiUrl;

    @Value("${ai.huggingface.models.image-captioning}")
    private String imageCaptioningModel;

    @Value("${ai.huggingface.token}")
    private String apiKey;

    public ImageCaptioningService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public String generateCaption(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return "No image provided";
        }

        try {
            String url = apiUrl + "/" + imageCaptioningModel;
            System.out.println("Using Hugging Face API URL: " + url);

            // Prepare multipart body with image file
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageFile.getBytes()) {
                @NotNull
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            });

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(apiKey);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return parseImageCaptionResponse(response.getBody());
            } else {
                return "Unable to generate caption (API error " + response.getStatusCodeValue() + ")";
            }

        } catch (Exception e) {
            return "Error processing image: " + e.getMessage();
        }
    }

    private String parseImageCaptionResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);

            // Array format
            if (jsonNode.isArray() && jsonNode.size() > 0 && jsonNode.get(0).has("generated_text")) {
                return jsonNode.get(0).get("generated_text").asText();
            }

            // Object format
            if (jsonNode.has("generated_text")) {
                return jsonNode.get("generated_text").asText();
            }

            if (jsonNode.has("error")) {
                return "API error: " + jsonNode.get("error").asText();
            }

            return "Unable to parse caption response";
        } catch (Exception e) {
            return "Error parsing caption response: " + e.getMessage();
        }
    }

    public String generateCaptionWithFallback(MultipartFile imageFile) {
        try {
            return generateCaption(imageFile);
        } catch (Exception e) {
            return generateBasicImageDescription(imageFile);
        }
    }

    private String generateBasicImageDescription(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return "No image provided";
        }

        String contentType = imageFile.getContentType();
        String filename = imageFile.getOriginalFilename();

        StringBuilder description = new StringBuilder("Image file");

        if (!filename.isEmpty()) {
            description.append(" named '").append(filename).append("'");
        }

        description.append(" of type ").append(contentType)
                .append(" (").append(imageFile.getSize()).append(" bytes)");

        return description.toString();
    }
}
