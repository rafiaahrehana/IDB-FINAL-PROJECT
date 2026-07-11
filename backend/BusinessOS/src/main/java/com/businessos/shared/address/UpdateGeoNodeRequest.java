package com.businessos.shared.address;

import lombok.Data;

@Data
public class UpdateGeoNodeRequest {
    private String name;
    private String code;
    private Long parentId;
}
