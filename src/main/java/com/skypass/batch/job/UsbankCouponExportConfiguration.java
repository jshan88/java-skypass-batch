package com.skypass.batch.job;

import com.skypass.batch.common.CommonService;
import com.skypass.batch.usbankCpn.CouponApplicant;
import com.skypass.batch.usbankCpn.CouponApplicantMethods;
import com.skypass.batch.usbankCpn.CouponApplicantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class UsbankCouponExportConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final CouponApplicantRepository couponApplicantRepository;
    private final CommonService commonUtil;

    private int chunkSize;
    @Value("${chunkSize:100}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Bean
    public Job usbankCouponJob() {
        return jobBuilderFactory.get("usbankCouponJob")
                .start(applicantToCsvStep())
                .build();
    }

    @Bean
    @JobScope
    public Step applicantToCsvStep() {
        return stepBuilderFactory.get("applicantToCsvStep")
                .<CouponApplicant, CouponApplicant>chunk(chunkSize)
                .reader(applicantsReader(null, null))
//                .processor(processor())
                .writer(jpaCursorItemWriter(null, null))
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<CouponApplicant> applicantsReader(@Value("#{jobParameters[requestDate]}") String requestDate,
                                                                 @Value("#{jobParameters[cardType]}") String cardType) {

        LocalDateTime created = commonUtil.stringToLocalDateTime(requestDate);

        Map<String,Object> parameterValues = new HashMap<>();
        parameterValues.put("created", created);
        parameterValues.put("cardType", cardType);

        return new JpaCursorItemReaderBuilder<CouponApplicant>()
                .name("applicantsReader")
                .entityManagerFactory(entityManagerFactory)
                .parameterValues(parameterValues)
                .queryString("select a from CouponApplicant a " +
                        "where a.created > :created " +
                        "and a.cardType = :cardType " +
                        "and a.cpnType = 'TD' " +
                        "and a.transFlg is null")
                .build();
    }
//    @Bean
//    public ItemProcessor<CouponApplicant, String[]> processor() {
//        log.info(">>>> processor step");
//        return item -> {
//            item.updateTransFlg("E");
//            return new CouponApplicantMethods().couponApplicantToArray(item);
//        };
//    }


    @Bean
    @StepScope
    public ItemWriter<CouponApplicant> jpaCursorItemWriter(@Value("#{jobParameters[requestDate]}") String requestDate,
                                                           @Value("#{jobParameters[cardType]}") String cardType) {
        log.info(">>>> writer step");
        CouponApplicantMethods couponApplicantMethods = new CouponApplicantMethods();
        List<String[]> csvLines = new ArrayList<>();
        LocalDateTime created = commonUtil.stringToLocalDateTime(requestDate);

        return items -> {
            for (CouponApplicant item: items) {                ;
                csvLines.add(couponApplicantMethods.couponApplicantToArray(item));
            }
            new CommonService().generateCsvFile(csvLines, "./sample.csv");
            couponApplicantRepository.updateTransFlag("E", created, cardType);
        };
    }
}
