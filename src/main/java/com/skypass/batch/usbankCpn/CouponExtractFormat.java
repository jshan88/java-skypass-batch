package com.skypass.batch.usbankCpn;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
public class CouponExtractFormat {

    private String ccId;
    private String mbrNo;
    private String custNm;
    private String currency;
    private String cpnAmt;
    private String discRate;
    private String validStartDate;
    private String validEndDate;

    @Builder
    public CouponExtractFormat(String ccId, String memberNum, String custNm, String currency,
                               String cpnAmt, String discRate, String validStartDate, String validEndDate)
    {
        this.ccId = ccId;
        this.mbrNo = memberNum;
        this.custNm = custNm;
        this.currency = currency;
        this.cpnAmt = cpnAmt;
        this.discRate = discRate;
        this.validStartDate = validStartDate;
        this.validEndDate = validEndDate;
    }
}
