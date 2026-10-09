
package com.example.splitbill.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.splitbill.model.Group;
import com.example.splitbill.repository.GroupRepository;
import com.example.splitbill.service.GroupService;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupRepository groupRepository;
private final GroupService groupService;

public GroupController(GroupRepository groupRepository,GroupService groupService) {
    this.groupRepository = groupRepository;
    this.groupService = groupService;
}

    // GET: ดูกลุ่มทั้งหมด
    @GetMapping
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    // GET: ดูกลุ่มตาม ID
    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        return groupRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: สร้างกลุ่ม
    @PostMapping
public ResponseEntity<Group> createGroup(@RequestBody Group group) {
    group.setId(null);
    Group savedGroup = groupService.saveGroupWithInviteCode(group);
    return ResponseEntity.ok(savedGroup);
}

    // PUT: แก้ไขข้อมูลกลุ่ม
    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(
            @PathVariable Long id,
            @RequestBody Group updatedGroup) {

        return groupRepository.findById(id)
                .map(group -> {
                    group.setName(updatedGroup.getName());
                    group.setDescription(updatedGroup.getDescription());

                    Group savedGroup = groupRepository.save(group);
                    return ResponseEntity.ok(savedGroup);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE: ลบกลุ่ม
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        if (!groupRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        groupRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}