package com.demo.eshop.repository;

import com.demo.eshop.domain.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    // 비관적 락을 적용한 조회 메서드
    // PESSImistic_WRITE : 내가 수정할꺼니까, 다른 사람은 다 막아.(강력한 락)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);
}
