package com.demo.eshop.service;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Long registerProduct(ProductDto.RegisterRequest requestDto){
        Product product = new Product(
                requestDto.getName(),
                requestDto.getPrice(),
                requestDto.getStockQuantity()
        );

        Product savedProduct = productRepository.save(product);

        return savedProduct.getId();
    }

    @Cacheable(value = "products", key = "#productId", cacheManager = "cacheManager")
    @Transactional(readOnly = true)
    public ProductDto.Response getProductById(Long productId) {
        Product product =  productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return new ProductDto.Response(product);
    }

    public List<Product> search(ProductDto.SearchCondition condition){
        return productRepository.search(condition);
    }
}
