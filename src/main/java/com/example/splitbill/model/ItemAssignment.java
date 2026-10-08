package com.example.splitbill.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_assignments")
public class ItemAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bill_item_id", nullable = false)
    private BillItem billItem;

    @ManyToOne
    @JoinColumn(name = "bill_member_id", nullable = false)
    private BillMember billMember;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    public ItemAssignment() {
    }

    public ItemAssignment(Long id, BillItem billItem,
                          BillMember billMember, BigDecimal amount) {
        this.id = id;
        this.billItem = billItem;
        this.billMember = billMember;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BillItem getBillItem() {
        return billItem;
    }

    public void setBillItem(BillItem billItem) {
        this.billItem = billItem;
    }

    public BillMember getBillMember() {
        return billMember;
    }

    public void setBillMember(BillMember billMember) {
        this.billMember = billMember;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
