package com.example.splitbill.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "settlements")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @ManyToOne
    @JoinColumn(name = "from_member_id", nullable = false)
    private BillMember fromMember;

    @ManyToOne
    @JoinColumn(name = "to_member_id", nullable = false)
    private BillMember toMember;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 50)
    private String status;

    private LocalDateTime settledAt;

    public Settlement() {
    }

    public Settlement(Long id, Bill bill,
                      BillMember fromMember,
                      BillMember toMember,
                      BigDecimal amount,
                      String status) {
        this.id = id;
        this.bill = bill;
        this.fromMember = fromMember;
        this.toMember = toMember;
        this.amount = amount;
        this.status = status;
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

    public BillMember getFromMember() {
        return fromMember;
    }

    public void setFromMember(BillMember fromMember) {
        this.fromMember = fromMember;
    }

    public BillMember getToMember() {
        return toMember;
    }

    public void setToMember(BillMember toMember) {
        this.toMember = toMember;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }
}
