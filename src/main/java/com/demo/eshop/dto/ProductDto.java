package com.demo.eshop.dto;

import com.demo.eshop.domain.Product;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ProductDto {
    @Getter
    @Setter
    public static class RegisterRequest {

        private String name;
        private int price;
        private int stockQuantity;

    }

    @Data
    public static class SearchCondition {
        private String name;
        private Integer minPrice;
        private Integer maxPrice;
    }

    @Getter
    @NoArgsConstructor
    public static class Response {

        private Long id;
        private String name;
        private int price;
        private int stockQuantity;

        public Response(Product product) {
            this.id = product.getId();
            this.name = product.getName();
            this.price = product.getPrice();
            this.stockQuantity = product.getStockQuantity();
        }
    }
}
