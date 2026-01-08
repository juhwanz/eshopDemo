package com.demo.eshop.repoLayer;


import com.demo.eshop.config.QueryDslConfig;
import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;
import com.demo.eshop.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
public class ProductRepoTest {
    // Product Repo, Order Repo
    // @DataJpaTest( 가벼운 인메모리 DB )
    // QueryDSL 설정 로드.
    //-> Dynamic Query : BooleanExpression 조합 (AND, OR, NUll 처리) 검증
    //-> JPA Query Method : findAllByUserOrderByIdDesc의 정렬 순서 및 필터링 확인.
    @Autowired
    ProductRepository productRepository;

    @Autowired
    EntityManager em;

    @BeforeEach
    void setUp(){
        productRepository.save(new Product("MacBook Pro", 3000000, 10));
        productRepository.save(new Product("MacBook Air", 1500000, 5));
        productRepository.save(new Product("iPhone 15", 1300000, 20));
        productRepository.save(new Product("Galaxy S24", 1200000, 15));
        productRepository.save(new Product("LG Gram", 1400000, 8));
    }

    @Test
    @DisplayName("검색: 이름 포함 조건 (MacBook)")
    void search_name(){
        // given
        ProductDto.SearchCondition condition = new ProductDto.SearchCondition();
        condition.setName("MacBook");

        // 👈 Pageable 생성 (0페이지, 10개씩)
        Pageable pageable = PageRequest.of(0, 10);

        // when
        // 👈 인자 추가
        Page<Product> result = productRepository.search(condition, pageable);

        // then
        // Page 객체 자체보다는 .getContent()로 내용물을 검증하는 것이 명확합니다.
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("name")
                .containsExactlyInAnyOrder("MacBook Pro", "MacBook Air");
    }

    // (선택 사항) 페이징 기능 자체도 잘 동작하는지 테스트 하나 추가하면 더 좋습니다!
    @Test
    @DisplayName("검색: 페이징 동작 확인 (5개 중 2개만 조회)")
    void search_paging(){
        // given
        ProductDto.SearchCondition condition = new ProductDto.SearchCondition(); // 조건 없음 (전체 조회)
        Pageable pageable = PageRequest.of(0, 2); // 0번 페이지, 2개만 가져오기

        // when
        Page<Product> result = productRepository.search(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(2); // 2개만 가져왔는지
        assertThat(result.getTotalElements()).isEqualTo(5); // 전체 개수는 5개인지 (Count Query 동작 확인)
        assertThat(result.hasNext()).isTrue(); // 다음 페이지가 있다고 나오는지
    }


}
