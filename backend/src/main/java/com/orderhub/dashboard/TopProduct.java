package com.orderhub.dashboard;

import java.math.BigDecimal;

public record TopProduct(Long productId, String sku, String name, Long unitsSold, BigDecimal revenue) {
}
