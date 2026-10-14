package com.orderhub.product;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ProductDtos {

    private ProductDtos() {
    }

    public record CreateProductRequest(
            @NotBlank
            @Size(max = 40)
            @Pattern(regexp = "[A-Za-z0-9-]+", message = "may contain only letters, digits and dashes")
            String sku,
            @NotBlank @Size(max = 160) String name,
            @Size(max = 2000) String description,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
            @Min(0) int stockQuantity,
            @Min(0) int reorderLevel
    ) {
    }

    public record UpdateProductRequest(
            @NotBlank @Size(max = 160) String name,
            @Size(max = 2000) String description,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
            @Min(0) int stockQuantity,
            @Min(0) int reorderLevel
    ) {
    }

    public record ProductResponse(
            Long id,
            String sku,
            String name,
            String description,
            BigDecimal price,
            int stockQuantity,
            int reorderLevel,
            boolean lowStock,
            Instant updatedAt
    ) {

        public static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getDescription(), p.getPrice(),
                    p.getStockQuantity(), p.getReorderLevel(), p.isLowStock(), p.getUpdatedAt());
        }
    }
}
