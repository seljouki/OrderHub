package com.orderhub.product;

import com.orderhub.common.exception.ConflictException;
import com.orderhub.common.exception.ResourceNotFoundException;
import com.orderhub.common.web.PageResponse;
import com.orderhub.product.ProductDtos.CreateProductRequest;
import com.orderhub.product.ProductDtos.ProductResponse;
import com.orderhub.product.ProductDtos.UpdateProductRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public PageResponse<ProductResponse> search(String search, Pageable pageable) {
        var page = StringUtils.hasText(search)
                ? products.search(search.trim(), pageable)
                : products.findByActiveTrue(pageable);
        return PageResponse.from(page, ProductResponse::from);
    }

    public ProductResponse get(Long id) {
        return ProductResponse.from(findActive(id));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        String sku = request.sku().trim().toUpperCase();
        if (products.existsBySkuIgnoreCase(sku)) {
            throw new ConflictException("A product with SKU %s already exists".formatted(sku));
        }
        Product product = new Product(sku, request.name().trim(), request.description(), request.price(),
                request.stockQuantity(), request.reorderLevel());
        return ProductResponse.from(products.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = findActive(id);
        product.update(request.name().trim(), request.description(), request.price(),
                request.stockQuantity(), request.reorderLevel());
        // flush so the response reflects the new updatedAt / version
        return ProductResponse.from(products.saveAndFlush(product));
    }

    /** Soft delete: products referenced by historical orders must remain in the database. */
    @Transactional
    public void deactivate(Long id) {
        findActive(id).deactivate();
    }

    private Product findActive(Long id) {
        return products.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
