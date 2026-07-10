package com.businessos.shared.location;

import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GeoNodeServiceImpl implements GeoNodeService {

    private final GeoNodeRepository geoNodeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GeoNodeDto> getCountries() {
        return geoNodeRepository.findByType(GeoNodeType.COUNTRY)
                .stream()
                .map(GeoNodeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeoNodeDto> getChildren(Long parentId) {
        if (parentId == null) {
            throw new BadRequestException("Parent ID is required to fetch children");
        }
        if (!geoNodeRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Parent node not found with ID: " + parentId);
        }
        return geoNodeRepository.findByParentId(parentId)
                .stream()
                .map(GeoNodeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GeoNodeDto getNodeById(Long id) {
        GeoNode node = geoNodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GeoNode not found with ID: " + id));
        return GeoNodeMapper.toDto(node);
    }

    @Override
    public GeoNodeDto createNode(CreateGeoNodeRequest request) {
        validateHierarchy(request.getType(), request.getParentId());

        GeoNode parent = null;
        if (request.getParentId() != null) {
            parent = geoNodeRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent node not found with ID: " + request.getParentId()));
        }

        GeoNode node = GeoNode.builder()
                .name(request.getName().trim())
                .type(request.getType())
                .code(request.getCode() != null ? request.getCode().trim().toUpperCase() : null)
                .parent(parent)
                .build();

        GeoNode saved = geoNodeRepository.save(node);
        return GeoNodeMapper.toDto(saved);
    }

    @Override
    public GeoNodeDto updateNode(Long id, UpdateGeoNodeRequest request) {
        GeoNode node = geoNodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GeoNode not found with ID: " + id));

        if (request.getName() != null) {
            node.setName(request.getName().trim());
        }
        if (request.getCode() != null) {
            node.setCode(request.getCode().trim().toUpperCase());
        }

        if (request.getParentId() != null) {
            if (id.equals(request.getParentId())) {
                throw new BadRequestException("A node cannot be its own parent");
            }
            validateHierarchy(node.getType(), request.getParentId());
            GeoNode parent = geoNodeRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent node not found with ID: " + request.getParentId()));
            node.setParent(parent);
        }

        GeoNode saved = geoNodeRepository.save(node);
        return GeoNodeMapper.toDto(saved);
    }

    @Override
    public void deleteNode(Long id) {
        GeoNode node = geoNodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GeoNode not found with ID: " + id));
        geoNodeRepository.delete(node);
    }

    private void validateHierarchy(GeoNodeType type, Long parentId) {
        if (type == GeoNodeType.COUNTRY) {
            if (parentId != null) {
                throw new BadRequestException("COUNTRY nodes cannot have a parent");
            }
        } else {
            if (parentId == null) {
                throw new BadRequestException("Nodes of type " + type + " must have a parent node");
            }
            GeoNode parent = geoNodeRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent node not found with ID: " + parentId));

            GeoNodeType expectedParentType;
            switch (type) {
                case LEVEL1:
                    expectedParentType = GeoNodeType.COUNTRY;
                    break;
                case LEVEL2:
                    expectedParentType = GeoNodeType.LEVEL1;
                    break;
                case LEVEL3:
                    expectedParentType = GeoNodeType.LEVEL2;
                    break;
                case LEVEL4:
                    expectedParentType = GeoNodeType.LEVEL3;
                    break;
                default:
                    throw new BadRequestException("Invalid node type: " + type);
            }

            if (parent.getType() != expectedParentType) {
                throw new BadRequestException("Parent node type must be " + expectedParentType + " for child of type " + type + ", but was " + parent.getType());
            }
        }
    }
}
