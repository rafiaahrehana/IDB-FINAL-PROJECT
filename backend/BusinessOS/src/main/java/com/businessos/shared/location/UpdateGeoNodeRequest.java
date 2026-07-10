package com.businessos.shared.location;

import lombok.Data;

@Data
public class UpdateGeoNodeRequest {
    private String name;
    private String code;
    private Long parentId;
}
