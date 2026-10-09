package com.example.splitbill.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.example.splitbill.model.Group;
import com.example.splitbill.repository.GroupRepository;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final SecureRandom random = new SecureRandom();

    private static final String CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public String generateUniqueInviteCode() {
        String code;

        do {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < 8; i++) {
                int index = random.nextInt(CHARACTERS.length());
                builder.append(CHARACTERS.charAt(index));
            }

            code = builder.toString();

        } while (groupRepository.existsByInviteCode(code));

        return code;
    }

    public Group saveGroupWithInviteCode(Group group) {
        group.setInviteCode(generateUniqueInviteCode());
        return groupRepository.save(group);
    }
}
