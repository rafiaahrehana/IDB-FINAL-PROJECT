package com.businessos.modules.hrm.designation;

public class DesignationMapper {

    public static DesignationResponse toDesignationResponse(Designation d) {
        DesignationResponse r = new DesignationResponse();
        r.setId(d.getId());
        r.setName(d.getName());
        r.setCode(d.getCode());
        r.setLevel(d.getLevel());
        r.setDescription(d.getDescription());
        r.setActive(d.isActive());
        r.setCreatedAt(d.getCreatedAt());
        return r;
    }
}
