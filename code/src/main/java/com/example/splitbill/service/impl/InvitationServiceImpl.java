package com.example.splitbill.service.impl;

import com.example.splitbill.dto.response.InvitationResponse;
import com.example.splitbill.exception.ConflictException;
import com.example.splitbill.exception.ForbiddenException;
import com.example.splitbill.exception.ResourceNotFoundException;
import com.example.splitbill.model.Bill;
import com.example.splitbill.model.BillMember;
import com.example.splitbill.model.BillStatus;
import com.example.splitbill.model.Invitation;
import com.example.splitbill.model.MemberRole;
import com.example.splitbill.model.Notification;
import com.example.splitbill.model.User;
import com.example.splitbill.repository.BillMemberRepository;
import com.example.splitbill.repository.BillRepository;
import com.example.splitbill.repository.InvitationRepository;
import com.example.splitbill.repository.NotificationRepository;
import com.example.splitbill.repository.UserRepository;
import com.example.splitbill.service.InvitationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InvitationServiceImpl implements InvitationService {
    private final InvitationRepository invitationRepository;
    private final BillRepository billRepository;
    private final BillMemberRepository billMemberRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public InvitationServiceImpl(InvitationRepository invitationRepository, BillRepository billRepository,
                                 BillMemberRepository billMemberRepository, UserRepository userRepository,
                                 NotificationRepository notificationRepository) {
        this.invitationRepository = invitationRepository;
        this.billRepository = billRepository;
        this.billMemberRepository = billMemberRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public InvitationResponse invite(Long billId, Long inviterId, Long inviteeId) {
        Bill bill = findBill(billId);
        requireMember(bill, inviterId);
        if (inviterId.equals(inviteeId)) {
            throw new ConflictException("ไม่สามารถเชิญตัวเองได้");
        }
        User invitee = findUser(inviteeId);
        if (isMember(bill, inviteeId)) {
            throw new ConflictException("ผู้ใช้นี้อยู่ในบิลแล้ว");
        }
        if (invitationRepository.existsByBill_IdAndInvitee_IdAndStatus(billId, inviteeId, Invitation.PENDING)) {
            throw new ConflictException("เชิญผู้ใช้นี้ไปแล้ว รอการตอบรับ");
        }

        User inviter = findUser(inviterId);
        Invitation invitation = new Invitation();
        invitation.setBill(bill);
        invitation.setInviter(inviter);
        invitation.setInvitee(invitee);
        invitation.setStatus(Invitation.PENDING);
        Invitation saved = invitationRepository.save(invitation);

        notificationRepository.save(new Notification(null, invitee, "INVITATION", "คำเชิญเข้าบิล",
                inviter.getUsername() + " เชิญคุณเข้าร่วมบิล: " + bill.getRestaurantName(), false));
        return toResponse(saved);
    }

    @Override @Transactional(readOnly = true)
    public List<InvitationResponse> myInvitations(Long userId) {
        return invitationRepository.findByInvitee_IdAndStatusOrderByCreatedAtDesc(userId, Invitation.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<InvitationResponse> pendingForBill(Long billId, Long requesterId) {
        requireMember(findBill(billId), requesterId);
        return invitationRepository.findByBill_IdAndStatusOrderByCreatedAtDesc(billId, Invitation.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public void accept(Long invitationId, Long userId) {
        Invitation invitation = findPendingForInvitee(invitationId, userId);
        Bill bill = invitation.getBill();
        // เหมือน BillServiceImpl.join(): เข้าร่วมบิลที่ถูกยกเลิกแล้วไม่ได้
        if (BillStatus.CANCELLED.equals(bill.getStatus())) {
            throw new ConflictException("บิลนี้ถูกยกเลิกแล้ว");
        }
        invitation.setStatus(Invitation.ACCEPTED);
        // อาจเข้าร่วมด้วยรหัสไปก่อนแล้ว ไม่ต้องเพิ่มซ้ำ
        if (!isMember(bill, userId)) {
            BillMember member = new BillMember();
            member.setBill(bill);
            member.setUser(invitation.getInvitee());
            member.setRole(MemberRole.MEMBER);
            billMemberRepository.save(member);
        }
    }

    @Override
    public void reject(Long invitationId, Long userId) {
        findPendingForInvitee(invitationId, userId).setStatus(Invitation.REJECTED);
    }

    private Invitation findPendingForInvitee(Long invitationId, Long userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + invitationId));
        if (!invitation.getInvitee().getId().equals(userId)) {
            throw new ForbiddenException("You are not the invitee of this invitation");
        }
        if (!Invitation.PENDING.equals(invitation.getStatus())) {
            throw new ConflictException("ตอบคำเชิญนี้ไปแล้ว");
        }
        return invitation;
    }

    private boolean isMember(Bill bill, Long userId) {
        return bill.getCreatedBy().getId().equals(userId)
                || billMemberRepository.existsByBill_IdAndUser_Id(bill.getId(), userId);
    }

    private void requireMember(Bill bill, Long userId) {
        if (!isMember(bill, userId)) {
            throw new ForbiddenException("You are not a member of this bill");
        }
    }

    private Bill findBill(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private InvitationResponse toResponse(Invitation invitation) {
        return new InvitationResponse(
                invitation.getId(), invitation.getBill().getId(), invitation.getBill().getRestaurantName(),
                invitation.getInviter().getId(), invitation.getInviter().getUsername(),
                invitation.getInvitee().getId(), invitation.getInvitee().getUsername(),
                invitation.getStatus(), invitation.getCreatedAt());
    }
}
