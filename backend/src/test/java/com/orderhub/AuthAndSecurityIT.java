package com.orderhub;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class AuthAndSecurityIT extends AbstractIntegrationTest {

    @Test
    void loginReturnsTokenAndUserProfile() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "ADMIN@orderhub.dev", "password": "Admin123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void wrongPasswordIsRejectedWithProblemDetail() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@orderhub.dev", "password": "nope"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Invalid credentials"));
    }

    @Test
    void protectedEndpointsRequireAToken() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedTokensAreRejected() throws Exception {
        String token = adminToken();
        String tampered = token.substring(0, token.length() - 4) + "abcd";

        mvc.perform(get("/api/products").header(HttpHeaders.AUTHORIZATION, tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsTheAuthenticatedUser() throws Exception {
        mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, staffToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(STAFF_EMAIL))
                .andExpect(jsonPath("$.role").value("STAFF"));
    }

    @Test
    void staffCannotManageTheCatalogue() throws Exception {
        mvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, staffToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku": "STAFF-1", "name": "Nope", "price": 1.00, "stockQuantity": 1, "reorderLevel": 0}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void healthAndApiDocsArePublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
