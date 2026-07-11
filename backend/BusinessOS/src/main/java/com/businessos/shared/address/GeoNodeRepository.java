package com.businessos.shared.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeoNodeRepository extends JpaRepository<GeoNode, Long> {
    List<GeoNode> findByType(GeoNodeType type);
    List<GeoNode> findByParentId(Long parentId);
}
