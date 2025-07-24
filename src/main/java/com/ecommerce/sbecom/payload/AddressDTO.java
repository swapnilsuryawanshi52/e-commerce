package com.ecommerce.sbecom.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long addressId;
    private String buildingName;
    private String street;
    private String city;
    private String state;
    private String country;
    private String pincode;
}
