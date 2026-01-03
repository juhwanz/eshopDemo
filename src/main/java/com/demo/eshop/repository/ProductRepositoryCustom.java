package com.demo.eshop.repository;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> search(ProductDto.SearchCondition condition);
}
