package com.businessos.auth.user;

import com.businessos.shared.address.AddressRequest;
import lombok.Data;

@Data
public class UserProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String image;
    private String languagePreference;
    private AddressRequest location;
}
