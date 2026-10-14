package com.orderhub.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    @Query("""
            select p from Product p
            where p.active = true
              and (lower(p.name) like lower(concat('%', :search, '%'))
                   or lower(p.sku) like lower(concat('%', :search, '%')))
            """)
    Page<Product> search(@Param("search") String search, Pageable pageable);

    @Query("select p from Product p where p.active = true and p.stockQuantity <= p.reorderLevel order by p.stockQuantity asc")
    List<Product> findLowStock();

    Page<Product> findByActiveTrue(Pageable pageable);

    long countByActiveTrue();
}
