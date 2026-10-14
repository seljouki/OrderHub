package com.orderhub;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/** End-to-end business scenarios through the HTTP API. */
class OrderFlowIT extends AbstractIntegrationTest {

    private String admin;
    private String staff;

    @BeforeEach
    void authenticate() throws Exception {
        admin = adminToken();
        staff = staffToken();
    }

    @Test
    void placingAnOrderReservesStockAndCancellingRestoresIt() throws Exception {
        long productId = createProduct(10);
        long customerId = createCustomer();

        String order = mvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": %d, "items": [{"productId": %d, "quantity": 3}], "notes": "Rush"}
                                """.formatted(customerId, productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber", startsWith("ORD-")))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(75.0))
                .andExpect(jsonPath("$.createdBy").value(STAFF_EMAIL))
                .andExpect(jsonPath("$.allowedTransitions", hasItem("CANCELLED")))
                .andReturn().getResponse().getContentAsString();
        long orderId = ((Number) JsonPath.read(order, "$.id")).longValue();

        assertStock(productId, 7);

        mvc.perform(patch("/api/orders/{id}/status", orderId)
                        .header(HttpHeaders.AUTHORIZATION, staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "CANCELLED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertStock(productId, 10);
    }

    @Test
    void orderingMoreThanAvailableIsRejectedAndLeavesStockUntouched() throws Exception {
        long productId = createProduct(2);
        long customerId = createCustomer();

        mvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": %d, "items": [{"productId": %d, "quantity": 5}]}
                                """.formatted(customerId, productId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Business rule violated"));

        assertStock(productId, 2);
    }

    @Test
    void illegalStatusChangesAreRejected() throws Exception {
        long productId = createProduct(5);
        long customerId = createCustomer();
        String order = mvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": %d, "items": [{"productId": %d, "quantity": 1}]}
                                """.formatted(customerId, productId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long orderId = ((Number) JsonPath.read(order, "$.id")).longValue();

        mvc.perform(patch("/api/orders/{id}/status", orderId)
                        .header(HttpHeaders.AUTHORIZATION, staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "DELIVERED"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void invalidPayloadsReturnFieldErrors() throws Exception {
        mvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku": "bad sku!", "name": "", "price": -1, "stockQuantity": 0, "reorderLevel": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sku").exists())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void duplicateSkuIsAConflict() throws Exception {
        String sku = "DUP-" + shortId();
        String payload = """
                {"sku": "%s", "name": "Thing", "price": 5.00, "stockQuantity": 1, "reorderLevel": 0}
                """.formatted(sku);

        mvc.perform(post("/api/products").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/products").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void dashboardSummarisesActivity() throws Exception {
        mvc.perform(get("/api/dashboard/summary").header(HttpHeaders.AUTHORIZATION, staff))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revenueLast30Days").exists())
                .andExpect(jsonPath("$.ordersByStatus.PENDING").exists())
                .andExpect(jsonPath("$.dailyRevenue.length()").value(14));
    }

    private long createProduct(int stock) throws Exception {
        String body = mvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku": "T-%s", "name": "Test product", "price": 25.00, "stockQuantity": %d, "reorderLevel": 1}
                                """.formatted(shortId(), stock)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long createCustomer() throws Exception {
        String body = mvc.perform(post("/api/customers")
                        .header(HttpHeaders.AUTHORIZATION, staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Test Co", "email": "buyer-%s@test.example"}
                                """.formatted(shortId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private void assertStock(long productId, int expected) throws Exception {
        mvc.perform(get("/api/products/{id}", productId).header(HttpHeaders.AUTHORIZATION, staff))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(expected));
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
