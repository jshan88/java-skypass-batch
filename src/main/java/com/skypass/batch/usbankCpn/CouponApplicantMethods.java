package com.skypass.batch.usbankCpn;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CouponApplicantMethods {

    public String[] couponApplicantToArray(CouponApplicant couponApplicant) {

        String[] array = new String[8];
        String ccId = "";
        switch(couponApplicant.getCardType()) {
            case "VB": ccId = "2101LATNAHLYH";
                break;
            case "VX": ccId = "2101LATNAHL6G";
                break;
            case "VZ": ccId = "2101LATNAHLNQ";
                break;
        }
        array[0] = ccId; //CCID
        array[1] = couponApplicant.getMemberNum(); //MBR_NO
        array[2] = ""; // CUSTNM
        array[3] = "USD"; // CURRENCY
        array[4] = couponApplicant.getCpnAmt().replaceAll("^0+(?!$)", ""); //CPN_AMT
        array[5] = ""; //DISC_RATE
        array[6] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")); //VALIDSTARTDATE
        array[7] = LocalDateTime.now().plusMonths(1L).format(DateTimeFormatter.ofPattern("yyyyMMdd")); //VALIDENDDATE

        return array;
    }
}
