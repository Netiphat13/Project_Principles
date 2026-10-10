package com.example.splitbill.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

// สถานะการจ่ายเงิน + สลิปของสมาชิกแต่ละคนในบิล (PENDING / PAID / OVERDUE)
@Entity
@Table(name = "bill_payments")
public class BillPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    // ชื่อตรงกับชื่อในรายการแบ่งของบิล (username ถ้ามีบัญชี)
    @Column(name = "member_name", nullable = false, length = 100)
    private String memberName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    // ชื่อไฟล์สลิปบน server (ไม่ใช่ path เต็ม)
    @Column(name = "slip_image", length = 255)
    private String slipImage;

    @Column(name = "slip_uploaded_at")
    private LocalDateTime slipUploadedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public BillPayment() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSlipImage() { return slipImage; }
    public void setSlipImage(String slipImage) { this.slipImage = slipImage; }

    public LocalDateTime getSlipUploadedAt() { return slipUploadedAt; }
    public void setSlipUploadedAt(LocalDateTime slipUploadedAt) { this.slipUploadedAt = slipUploadedAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
