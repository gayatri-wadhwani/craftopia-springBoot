package com.example.craftopia.Service.ai;

import com.example.craftopia.Entity.ArtStyle;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class ArtStyleDetectionService {

    private static final Map<ArtStyle, String[]> ART_STYLE_KEYWORDS = new HashMap<>();

    static {
        ART_STYLE_KEYWORDS.put(ArtStyle.WARLI, new String[]{
                "warli", "tribal", "stick figure", "geometric", "circle", "triangle", "simple", "minimalist",
                "white on brown", "rice paste", "mud wall", "maharashtra", "adivasi", "primitive"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.GOND, new String[]{
                "gond", "dots", "patterns", "intricate", "detailed", "colorful", "nature", "animals",
                "trees", "madhya pradesh", "tribal art", "fine lines", "decorative"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.MADHUBANI, new String[]{
                "madhubani", "mithila", "bihar", "fish", "peacock", "lotus", "geometric patterns",
                "bright colors", "natural dyes", "religious", "mythological", "border designs"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.PATTACHITRA, new String[]{
                "pattachitra", "odisha", "cloth painting", "jagannath", "krishna", "religious themes",
                "natural colors", "fine brushwork", "mythological", "traditional"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.KALAMKARI, new String[]{
                "kalamkari", "pen work", "hand painted", "natural dyes", "cotton", "silk",
                "floral", "paisley", "andhra pradesh", "telangana", "block print"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.TANJORE, new String[]{
                "tanjore", "thanjavur", "gold foil", "precious stones", "religious", "gods", "goddesses",
                "rich colors", "embossed", "traditional", "south indian", "temple art"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.PICHWAI, new String[]{
                "pichwai", "krishna", "radha", "rajasthan", "temple hanging", "devotional",
                "intricate", "detailed", "religious", "spiritual", "traditional"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.MATA_NI_PACHEDI, new String[]{
                "mata ni pachedi", "goddess", "temple cloth", "gujarat", "hand block print",
                "natural dyes", "religious", "devotional", "traditional", "folk art"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.KALIGHAT, new String[]{
                "kalighat", "bengal", "kolkata", "simple lines", "bold", "social commentary",
                "everyday life", "watercolor", "pat painting", "folk art"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.BHIL, new String[]{
                "bhil", "tribal", "dots", "vibrant colors", "nature", "animals", "trees",
                "madhya pradesh", "rajasthan", "folk art", "traditional"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.CONTEMPORARY, new String[]{
                "contemporary", "modern", "abstract", "mixed media", "experimental", "fusion",
                "new age", "innovative", "current", "present day"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.TRADITIONAL, new String[]{
                "traditional", "classical", "heritage", "ancient", "old", "conventional",
                "time-honored", "customary", "established", "historic"
        });

        ART_STYLE_KEYWORDS.put(ArtStyle.FOLK, new String[]{
                "folk", "rural", "village", "community", "cultural", "ethnic", "indigenous",
                "local", "regional", "grassroots", "people's art"
        });
    }

    public ArtStyle detectArtStyle(MultipartFile image, String textContent, String imageCaption) {
        StringBuilder combinedContent = new StringBuilder();

        if (textContent != null && !textContent.trim().isEmpty()) {
            combinedContent.append(textContent.toLowerCase()).append(" ");
        }

        if (imageCaption != null && !imageCaption.trim().isEmpty()) {
            combinedContent.append(imageCaption.toLowerCase()).append(" ");
        }

        String content = combinedContent.toString().trim();

        if (content.isEmpty()) {
            return ArtStyle.UNKNOWN;
        }

        Map<ArtStyle, Integer> styleScores = new HashMap<>();

        for (Map.Entry<ArtStyle, String[]> entry : ART_STYLE_KEYWORDS.entrySet()) {
            ArtStyle style = entry.getKey();
            String[] keywords = entry.getValue();
            int score = 0;

            for (String keyword : keywords) {
                if (content.contains(keyword)) {
                    if (content.contains(" " + keyword + " ") ||
                            content.startsWith(keyword + " ") ||
                            content.endsWith(" " + keyword)) {
                        score += 3;
                    } else {
                        score += 1;
                    }
                }
            }

            if (score > 0) {
                styleScores.put(style, score);
            }
        }

        return styleScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ArtStyle.UNKNOWN);
    }

    public ArtStyle detectArtStyleFromText(String textContent) {
        return detectArtStyle(null, textContent, null);
    }

    public ArtStyle detectArtStyleFromImage(MultipartFile image, String imageCaption) {
        return detectArtStyle(image, null, imageCaption);
    }

    public ArtStyle detectArtStyleWithFallback(MultipartFile image, String textContent, String imageCaption) {
        try {
            return detectArtStyle(image, textContent, imageCaption);
        } catch (Exception e) {
            return inferArtStyleFromBasicKeywords(textContent, imageCaption);
        }
    }

    private ArtStyle inferArtStyleFromBasicKeywords(String textContent, String imageCaption) {
        StringBuilder combinedContent = new StringBuilder();

        if (textContent != null) {
            combinedContent.append(textContent.toLowerCase()).append(" ");
        }

        if (imageCaption != null) {
            combinedContent.append(imageCaption.toLowerCase()).append(" ");
        }

        String content = combinedContent.toString();

        if (content.contains("warli")) return ArtStyle.WARLI;
        if (content.contains("gond")) return ArtStyle.GOND;
        if (content.contains("madhubani")) return ArtStyle.MADHUBANI;
        if (content.contains("pattachitra")) return ArtStyle.PATTACHITRA;
        if (content.contains("kalamkari")) return ArtStyle.KALAMKARI;
        if (content.contains("tanjore") || content.contains("thanjavur")) return ArtStyle.TANJORE;
        if (content.contains("pichwai")) return ArtStyle.PICHWAI;
        if (content.contains("mata ni pachedi")) return ArtStyle.MATA_NI_PACHEDI;
        if (content.contains("kalighat")) return ArtStyle.KALIGHAT;
        if (content.contains("bhil")) return ArtStyle.BHIL;
        if (content.contains("folk")) return ArtStyle.FOLK;
        if (content.contains("traditional")) return ArtStyle.TRADITIONAL;
        if (content.contains("contemporary") || content.contains("modern")) return ArtStyle.CONTEMPORARY;

        return ArtStyle.UNKNOWN;
    }
}
