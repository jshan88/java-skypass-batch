package com.skypass.batch.job;

import com.skypass.batch.common.CommonUtil;
import com.skypass.batch.usbankCpn.CouponApplicant;
import com.skypass.batch.usbankCpn.CouponApplicantMethods;
import com.skypass.batch.usbankCpn.CouponApplicantRepository;
import com.skypass.batch.usbankCpn.CouponExtractFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final CommonUtil commonUtil;

    private int chunkSize;
    @Value("${chunkSize:100}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Bean
    public Job usbankCouponJob() {
        return jobBuilderFactory.get("usbankCouponJob")
                .start(applicantToCsvStep())
                .next(updateExportFlagStep(null, null))
                .build();
    }

    @Bean
    @JobScope
    public Step applicantToCsvStep() {
        log.info(">>>> 'applicantToCsvStep' starts...");
        return stepBuilderFactory.get("applicantToCsvStep")
//                .<CouponApplicant, CouponApplicant>chunk(chunkSize)
                .<CouponApplicant, CouponExtractFormat>chunk(chunkSize)
                .reader(applicantsReader(null, null))
                .processor(csvFormatProcessor())
//                .writer(jpaCursorItemWriter(null, null))
                .writer(csvWriter(null, null))
                .build();

    }

    @Bean
    @JobScope
    public Step updateExportFlagStep(@Value("#{jobParameters[requestDate]}") String requestDate,
                                     @Value("#{jobParameters[cardType]}") String cardType) {

        log.info(">>>> 'updateExportFlagStep' starts...");

        LocalDateTime created = commonUtil.stringToLocalDateTime(requestDate);
        return stepBuilderFactory.get("updateExportFlagStep")
                .tasklet((contribution, chunkContext) -> {
                    couponApplicantRepository.updateTransFlag("E", created, cardType);
                    return RepeatStatus.FINISHED;
                })
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

        log.info(">>>> 'applicantToCsvStep' - reading Applicants : ");

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
//    @StepScope
//    public ItemWriter<CouponApplicant> jpaCursorItemWriter(@Value("#{jobParameters[requestDate]}") String requestDate,
//                                                           @Value("#{jobParameters[cardType]}") String cardType) {
//        log.info(">>>> writer step");
//        CouponApplicantMethods couponApplicantMethods = new CouponApplicantMethods();
//        List<String[]> csvLines = new ArrayList<>();
//        LocalDateTime created = commonUtil.stringToLocalDateTime(requestDate);
//        log.info("OPEN WRITER START : " + LocalDateTime.now());
//        return items -> {
//            for (CouponApplicant item: items) {
//                csvLines.add(couponApplicantMethods.couponApplicantToArray(item));
//            }
//            new CommonUtil().generateCsvFileUsingOpenCsv(csvLines, "./sample.csv");
////            couponApplicantRepository.updateTransFlag("E", created, cardType);
//        };
//    }

    @Bean
    public ItemProcessor<CouponApplicant, CouponExtractFormat> csvFormatProcessor() {
        log.info(">>>> 'applicantToCsvStep' - processing Applicants to the Extract Format");
        return item -> {
            String ccId = "";
            switch(item.getCardType()) {
                case "VB": ccId = "2101LATNAHLYH";
                    break;
                case "VX": ccId = "2101LATNAHL6G";
                    break;
                case "VZ": ccId = "2101LATNAHLNQ";
                    break;
            }
            return CouponExtractFormat.builder()
                    .ccId(ccId)
                    .memberNum(item.getMemberNum())
                    .custNm("")
                    .currency("USD")
                    .cpnAmt(item.getCpnAmt().replaceAll("^0+(?!$)", ""))
                    .discRate("")
                    .validStartDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .validEndDate(LocalDateTime.now().plusMonths(1L).format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .build();
        };
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CouponExtractFormat> csvWriter(@Value("#{jobParameters[requestDate]}") String requestDate,
                                                                  @Value("#{jobParameters[cardType]}") String cardType) {

        log.info(">>>> 'applicantToCsvStep' - writing CSV & export");

        return new FlatFileItemWriterBuilder<CouponExtractFormat>()
                .name("flatFileItemWriter")
                .resource(new FileSystemResource("./sample_1.csv"))
                .delimited()
                .delimiter(",")
                .names(new String[] {"ccId", "mbrNo", "custNm", "currency", "cpnAmt", "discRate", "validStartDate", "validEndDate"})
                .build();
    }
}
