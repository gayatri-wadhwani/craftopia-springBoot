package com.example.craftopia.Service;

import com.example.craftopia.DTO.AIGeneratedProductDetails;
import com.example.craftopia.DTO.ProductRequest;
import com.example.craftopia.DTO.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
public class AIOrchestrationService {

    @Autowired
    public GeminiProductEnrichmentService geminiService;

    @Autowired
    public ProductService productService;

    public ProductResponse generateProductMetadata(
            MultipartFile image,
            String text,
            Double price,
            String imageUrl
    ) throws IOException {

        String imageBase64 = Base64.getEncoder().encodeToString(image.getBytes());

        // Get enriched product details from Gemini
        AIGeneratedProductDetails aiDetails = geminiService.enrichProduct(imageBase64, text);
        // Get translated text
        String translatedCaption = geminiService.translateText(text);

        System.out.println(translatedCaption);

        // Build ProductRequest to pass to ProductService
        ProductRequest productRequest = ProductRequest.builder()
                .name(aiDetails.getEnglishTitle())
                .description(aiDetails.getEnglishDescription())
                .price(price)
                .category(aiDetails.getCategory())
                .imageUrl(imageUrl)
                .tags(aiDetails.getTags())
                .style(aiDetails.getOccasion())
                .originalLanguageText(text)
                .translatedText(translatedCaption)
                .build();

        return productService.createProduct(productRequest);
    }
}
