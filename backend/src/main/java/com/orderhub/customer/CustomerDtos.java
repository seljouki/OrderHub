package com.orderhub.customer;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CustomerDtos {

    private CustomerDtos() {
    }

    /** Used for both create and update: every field is replaced. */
    public record CustomerRequest(
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Email @Size(max = 255) String email,
            @Size(max = 40) String phone,
            @Size(max = 120) String city
    ) {
    }

    public record CustomerResponse(Long id, String name, String email, String phone, String city, Instant createdAt) {

        public static CustomerResponse from(Customer c) {
            return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getCity(),
                    c.getCreatedAt());
        }
    }
}
