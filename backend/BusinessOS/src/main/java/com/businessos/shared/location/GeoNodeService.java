package com.businessos.shared.location;

import java.util.List;

public interface GeoNodeService {
    List<GeoNodeDto> getCountries();
    List<GeoNodeDto> getChildren(Long parentId);
    GeoNodeDto getNodeById(Long id);
    GeoNodeDto createNode(CreateGeoNodeRequest request);
    GeoNodeDto updateNode(Long id, UpdateGeoNodeRequest request);
    void deleteNode(Long id);
}
