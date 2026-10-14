package com.orderhub.dashboard;

import java.math.BigDecimal;
import java.time.Instant;

/** Lightweight projection used to build the revenue time series. */
public record OrderTotal(Instant createdAt, BigDecimal totalAmount) {
}
