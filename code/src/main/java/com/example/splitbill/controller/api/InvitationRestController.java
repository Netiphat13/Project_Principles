package com.example.splitbill.controller.api;

import com.example.splitbill.config.WebConfig;
import com.example.splitbill.dto.request.InvitationRequest;
import com.example.splitbill.dto.response.InvitationResponse;
import com.example.splitbill.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class InvitationRestController {
    private final InvitationService service;

    public InvitationRestController(InvitationService service) {
        this.service = service;
    }

    // เชิญผู้ใช้เข้าบิล (ต้องเป็นสมาชิกของบิลนั้น)
    @PostMapping("/bills/{billId}/invitations")
    public InvitationResponse invite(@PathVariable Long billId, @Valid @RequestBody InvitationRequest request,
                                     @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.invite(billId, userId, request.inviteeId());
    }

    @GetMapping("/bills/{billId}/invitations")
    public List<InvitationResponse> pendingForBill(@PathVariable Long billId,
                                                   @SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.pendingForBill(billId, userId);
    }

    // คำเชิญที่ฉันได้รับ (แสดงที่กระดิ่งแจ้งเตือน)
    @GetMapping("/invitations")
    public List<InvitationResponse> myInvitations(@SessionAttribute(WebConfig.USER_ID) Long userId) {
        return service.myInvitations(userId);
    }

    @PutMapping("/invitations/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        service.accept(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/invitations/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @SessionAttribute(WebConfig.USER_ID) Long userId) {
        service.reject(id, userId);
        return ResponseEntity.noContent().build();
    }
}
