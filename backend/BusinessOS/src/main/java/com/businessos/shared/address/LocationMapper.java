package com.businessos.shared.address;

import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public Location toEntity(LocationRequest request) {
        if (request == null) {
            return null;
        }

        return Location.builder()
                .country(request.getCountry())
                .level1(request.getLevel1())
                .level2(request.getLevel2())
                .level3(request.getLevel3())
                .level4(request.getLevel4())
                .streetAddress(request.getStreetAddress())
                .postalCode(request.getPostalCode())
                .apartment(request.getApartment())
                .build();
    }

    public LocationResponse toResponse(Location entity) {
        if (entity == null) {
            return null;
        }

        return LocationResponse.builder()
                .id(entity.getId())
                .country(entity.getCountry())
                .level1(entity.getLevel1())
                .level2(entity.getLevel2())
                .level3(entity.getLevel3())
                .level4(entity.getLevel4())
                .streetAddress(entity.getStreetAddress())
                .postalCode(entity.getPostalCode())
                .apartment(entity.getApartment())
                .build();
    }

    public void updateEntityFromRequest(Location entity, LocationRequest request) {
        if (entity == null || request == null) {
            return;
        }
        
        entity.setCountry(request.getCountry());
        entity.setLevel1(request.getLevel1());
        entity.setLevel2(request.getLevel2());
        entity.setLevel3(request.getLevel3());
        entity.setLevel4(request.getLevel4());
        entity.setStreetAddress(request.getStreetAddress());
        entity.setPostalCode(request.getPostalCode());
        entity.setApartment(request.getApartment());
    }
}
