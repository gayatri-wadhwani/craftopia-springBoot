package com.example.craftopia.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetails {
    private String orderId;
    private String currency;
    private String key;
    private Integer amount;
}

