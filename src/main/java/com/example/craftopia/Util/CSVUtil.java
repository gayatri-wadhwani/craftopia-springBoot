package com.example.craftopia.Util;

import com.example.craftopia.DTO.ProductRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CSVUtil {

    public static List<ProductRequest> parseCSV(MultipartFile file) throws IOException {
        List<ProductRequest> products = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }

                String[] values = line.split(",");
                ProductRequest product = ProductRequest.builder()
                        .name(values[0].trim())
                        .description(values[1].trim())
                        .price(Double.parseDouble(values[2].trim()))
                        .category(values[3].trim())
                        .imageUrl(values[4].trim())
                        .build();
                products.add(product);
            }
        }

        return products;
    }
}

