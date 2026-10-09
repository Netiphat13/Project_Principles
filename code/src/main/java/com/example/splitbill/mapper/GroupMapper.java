package com.example.splitbill.mapper;

import com.example.splitbill.dto.response.GroupResponse;
import com.example.splitbill.model.Group;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {
    public GroupResponse toResponse(Group group) {
        return new GroupResponse(
                group.getId(), group.getName(), group.getDescription(),
                group.getCreatedBy().getId(), group.getCreatedBy().getUsername(),
                group.getMembers().size(), group.getCreatedAt()
        );
    }
}
