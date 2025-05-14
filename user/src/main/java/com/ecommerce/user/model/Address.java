package com.ecommerce.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
public class Address {
    private String id;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipcode;
}
