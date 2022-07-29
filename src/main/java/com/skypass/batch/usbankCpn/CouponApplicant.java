package com.skypass.batch.usbankCpn;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name="BAT_BANK_US_KAL_BN_SUC42", schema="BATUSER")
@NoArgsConstructor
public class CouponApplicant {

    @Id
    @Column(name="ROWID", insertable = false)
    private String rowId;

    @Column(name="CARD_TYP")
    private String cardType;

    @Column(name="CPN_TYP")
    private String cpnType;

    @Column(name="MBR_NO")
    private String memberNum;

    @Column(name="CPN_AMT")
    private String cpnAmt;

    @Column(name="CREATED", insertable = false)
    private LocalDateTime created;

    @Column(name="TRANS_FLG")
    private String transFlg;

    @Builder
    public CouponApplicant(String rowId, String cardType, String cpnType, String memberNum, String cpnAmt, LocalDateTime created, String transFlg) {
        this.rowId = rowId;
        this.cardType = cardType;
        this.cpnType = cpnType;
        this.memberNum = memberNum;
        this.cpnAmt = cpnAmt;
        this.created = created;
        this.transFlg = transFlg;
    }

    public void updateTransFlg(String flag) {
        this.transFlg = flag;
    }
}
