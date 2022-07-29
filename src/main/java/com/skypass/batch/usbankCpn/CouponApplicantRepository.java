package com.skypass.batch.usbankCpn;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponApplicantRepository extends JpaRepository<CouponApplicant, String> {

    @Query("select a from CouponApplicant a where a.created > ?1 and a.cardType = ?2 and a.cpnType = 'TD' and a.transFlg is null")
    public List<CouponApplicant> findApplicants(LocalDateTime created, String cardType);

    @Modifying
    @Query("update CouponApplicant a " +
            "set a.transFlg = :transFlg " +
            "where a.created > :created " +
            "and a.cardType = :cardType " +
            "and a.cpnType = 'TD' " +
            "and a.transFlg is null")
    public void updateTransFlag(@Param("transFlg") String transFlg, @Param("created") LocalDateTime created, @Param("cardType") String cardType);
}
