package com.example.splitbill.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bill_members")
public class BillMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    // NULL ได้ ถ้าเป็น Guest
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String guestName;

    @Column(unique = true, length = 255)
    private String guestToken;

    private LocalDateTime joinedAt;

    @OneToMany(mappedBy = "billMember")
    private List<ItemAssignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "billMember")
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "fromMember")
    private List<Settlement> settlementsFrom = new ArrayList<>();

    @OneToMany(mappedBy = "toMember")
    private List<Settlement> settlementsTo = new ArrayList<>();

    public BillMember() {
    }

    public BillMember(Long id, Bill bill, User user,
                      String guestName, String guestToken) {
        this.id = id;
        this.bill = bill;
        this.user = user;
        this.guestName = guestName;
        this.guestToken = guestToken;
    }

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestToken() {
        return guestToken;
    }

    public void setGuestToken(String guestToken) {
        this.guestToken = guestToken;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public List<ItemAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<ItemAssignment> assignments) {
        this.assignments = assignments;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<Settlement> getSettlementsFrom() {
        return settlementsFrom;
    }

    public void setSettlementsFrom(List<Settlement> settlementsFrom) {
        this.settlementsFrom = settlementsFrom;
    }

    public List<Settlement> getSettlementsTo() {
        return settlementsTo;
    }

    public void setSettlementsTo(List<Settlement> settlementsTo) {
        this.settlementsTo = settlementsTo;
    }
}
