package com.orderhub.order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.orderhub.dashboard.OrderTotal;
import com.orderhub.dashboard.StatusCount;
import com.orderhub.dashboard.TopProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<SalesOrder, Long> {

    @EntityGraph(attributePaths = "customer")
    Page<SalesOrder> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = "customer")
    Page<SalesOrder> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "items", "items.product"})
    Optional<SalesOrder> findWithDetailsById(Long id);

    @Query("select new com.orderhub.dashboard.StatusCount(o.status, count(o)) "
            + "from SalesOrder o group by o.status")
    List<StatusCount> countGroupedByStatus();

    @Query("select new com.orderhub.dashboard.OrderTotal(o.createdAt, o.totalAmount) "
            + "from SalesOrder o where o.status <> :excluded and o.createdAt >= :since")
    List<OrderTotal> findOrderTotalsSince(@Param("since") Instant since, @Param("excluded") OrderStatus excluded);

    @Query("""
            select new com.orderhub.dashboard.TopProduct(
                p.id, p.sku, p.name, sum(i.quantity), sum(i.lineTotal))
            from OrderItem i join i.product p join i.order o
            where o.status <> :excluded
            group by p.id, p.sku, p.name
            order by sum(i.quantity) desc
            """)
    List<TopProduct> findTopProducts(@Param("excluded") OrderStatus excluded, Pageable pageable);
}
