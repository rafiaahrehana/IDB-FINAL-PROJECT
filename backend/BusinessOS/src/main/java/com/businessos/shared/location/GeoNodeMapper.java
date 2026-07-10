package com.businessos.shared.location;

public final class GeoNodeMapper {

    private GeoNodeMapper() {}

    public static GeoNodeDto toDto(GeoNode node) {
        if (node == null) {
            return null;
        }
        return new GeoNodeDto(
            node.getId(), 
            node.getName(), 
            node.getType() != null ? node.getType().name() : null, 
            node.getCode()
        );
    }
}
