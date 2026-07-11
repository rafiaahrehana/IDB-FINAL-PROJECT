package com.businessos.shared.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoNodeDto {
    private Long id;
    private String name;
    private String type;
    private String code;
}
