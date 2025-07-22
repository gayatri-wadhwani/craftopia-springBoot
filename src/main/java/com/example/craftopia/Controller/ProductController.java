package com.example.craftopia.Controller;

import com.example.craftopia.DTO.ProductRequest;
import com.example.craftopia.DTO.ProductResponse;
import com.example.craftopia.DTO.ProductUpdateRequest;
import com.example.craftopia.Service.CloudinaryService;
import com.example.craftopia.Service.ProductService;
import com.example.craftopia.Service.AIOrchestrationService;
import com.example.craftopia.Util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ProductController {

    @Autowired
    public ProductService service;
    @Autowired private AIOrchestrationService aiOrchestrationService;
    @Autowired private SecurityUtil securityUtil;
    @Autowired private CloudinaryService cloudinaryService;

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> create(
            @RequestParam("image") MultipartFile image,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "style", required = false) String style,
            @RequestParam(value = "originalLanguageText", required = false) String originalLanguageText,
            @RequestParam(value = "translatedText", required = false) String translatedText
    ) {
        try {
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().body("Image file is required.");
            }

            // Upload image and enrich metadata
            String imageUrl = cloudinaryService.uploadImage(image);

            ProductRequest request = ProductRequest.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .category(category)
                    .imageUrl(imageUrl)
                    .tags(tags)
                    .style(style)
                    .originalLanguageText(originalLanguageText)
                    .translatedText(translatedText)
                    .build();

            ProductResponse response = service.createProduct(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Product creation failed: " + e.getMessage());
        }
    }


    @PostMapping("/bulk-json")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> bulkCreateJson(@RequestBody List<ProductRequest> productList) {
        try {
            return ResponseEntity.ok(service.bulkCreateProducts(productList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Bulk creation failed: " + e.getMessage());
        }
    }


    @PostMapping("/bulk-csv")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> bulkCreateFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(service.bulkCreateProductsFromCSV(file));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CSV processing failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name="search", required = false) String search
    ) {
        try {
            return ResponseEntity.ok(service.getAllProducts(category, search));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch products "+e.getMessage());
        }
    }


    @GetMapping("/my-products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> getSellerProducts() {
        try {
            List<ProductResponse> sellerProducts = service.getProductsBySeller();
            return ResponseEntity.ok(sellerProducts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch seller products: " + e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(service.getProductById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found "+e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> partialUpdate(
            @PathVariable("id") Long id,
            @RequestBody ProductUpdateRequest dto) {
        try {
            return ResponseEntity.ok(service.partialUpdateProduct(id, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Failed to update product: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        try {
            service.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found or already deleted "+e.getMessage());
        }
    }

    @PostMapping("/ai/auto-fill")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> autoFillProduct(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String text,
            @RequestParam("price") Double price) {

        try {
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().body("Image file is required.");
            }

            System.out.println(name);
            System.out.println(text);
            String imageUrl = cloudinaryService.uploadImage(image);

            ProductResponse response = aiOrchestrationService.generateProductMetadata(
                    image, text, price, imageUrl
            );
            System.out.println(response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI autofill failed: " + e.getMessage());
        }
    }
}
