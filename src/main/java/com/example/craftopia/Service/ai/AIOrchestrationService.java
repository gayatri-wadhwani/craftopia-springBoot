package com.example.craftopia.Service.ai;
import com.example.craftopia.DTO.ProductResponse;
import com.example.craftopia.Entity.ArtStyle;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Service
public class AIOrchestrationService {

    private final ImageCaptioningService imageCaptioningService;
    private final LanguageDetectionService languageDetectionService;
    private final TranslationService translationService;
    private final TagGenerationService tagGenerationService;
    private final ArtStyleDetectionService artStyleDetectionService;
    private final SummaryGenerationService summaryGenerationService;

    public AIOrchestrationService(
            ImageCaptioningService imageCaptioningService,
            LanguageDetectionService languageDetectionService,
            TranslationService translationService,
            TagGenerationService tagGenerationService,
            ArtStyleDetectionService artStyleDetectionService,
            SummaryGenerationService summaryGenerationService) {
        this.imageCaptioningService = imageCaptioningService;
        this.languageDetectionService = languageDetectionService;
        this.translationService = translationService;
        this.tagGenerationService = tagGenerationService;
        this.artStyleDetectionService = artStyleDetectionService;
        this.summaryGenerationService = summaryGenerationService;
    }

    public ProductResponse generateProductMetadata(String sellerEmail, MultipartFile image, String userText, Double price, String imageUrl) {
        String caption = imageCaptioningService.generateCaption(image);
        String detectedLang = languageDetectionService.detectLanguage(userText);
        System.out.println(detectedLang);
        String translatedText = detectedLang.equals("en") ? userText : translationService.translateToEnglish(userText, detectedLang);
        System.out.println(translatedText);

        String combinedInput = (translatedText != null ? translatedText : "") + " " + (caption != null ? caption : "");

        List<String> tags = tagGenerationService.generateTags(combinedInput);
        ArtStyle style = artStyleDetectionService.detectArtStyle(image, translatedText, caption);
        String title = summaryGenerationService.generateTitle(combinedInput, style, tags);
        String description = summaryGenerationService.generateDescription(combinedInput, style, tags, caption);
        String category = detectCategory(style, tags);

        return ProductResponse.builder()
                .name(title)
                .description(description)
                .category(category)
                .price(price)
                .imageUrl(imageUrl)
                .sellerEmail(sellerEmail)
                .tags(tags)
                .style(style.getDisplayName())
                .originalLanguageText(userText)
                .translatedText(translatedText)
                .build();
    }

    private String detectCategory(ArtStyle style, List<String> tags) {
        if (style == null) return "Artwork";
        switch (style.getDisplayName().toLowerCase()) {
            case "warli": case "gond": case "madhubani": return "Folk Art";
            case "kalighat": return "Traditional Painting";
            case "contemporary": case "modern": return "Contemporary Art";
            default: break;
        }
        if (tags != null && tags.contains("gift")) return "Gifts & Souvenirs";
        return "Artwork";
    }
}
