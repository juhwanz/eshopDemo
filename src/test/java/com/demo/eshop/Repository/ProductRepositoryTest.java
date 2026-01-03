package com.demo.eshop.Repository;

import com.demo.eshop.config.QueryDslConfig;
import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;
import com.demo.eshop.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("QueryDSL 동적 쿼리 검색 테스트")
    void search() {

        productRepository.save(new Product("맛있는 과자", 1000, 10));
        productRepository.save(new Product("맛없는 과자", 2000, 10));
        productRepository.save(new Product("비싼 컴퓨터", 100000, 10));

        ProductDto.SearchCondition cond1 = new ProductDto.SearchCondition();
        cond1.setName("과자");
        List<Product> result1 = productRepository.search(cond1);

        assertThat(result1).hasSize(2);

        ProductDto.SearchCondition cond2 = new ProductDto.SearchCondition();
        cond2.setMinPrice(5000);
        List<Product> result2 = productRepository.search(cond2);

        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getName()).isEqualTo("비싼 컴퓨터");
    }
}