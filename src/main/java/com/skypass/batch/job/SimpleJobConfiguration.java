package com.skypass.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration  // Spring Batch의 모든 Job은 @Configuration으로 등록해서 사용
public class SimpleJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job simpleJob() {
        return jobBuilderFactory.get("simpleJob") // simpleJob 이란 이름의 Batch Job을 생성
                .start(simpleStep1(null))
                .next(simpleStep2(null))
                .build();
    }

    @Bean
    @JobScope
    public Step simpleStep1(@Value("#{jobParameters[requestDate]}") String requestDate) {

        return stepBuilderFactory.get("simpleStep1") // simpleStep1 이란 이름의 Batch Step을 생성
                .tasklet((contribution, chunkContext) -> {  // Step 안에서 수행될 기능들을 명시. Tasklet은 Step안에서 단일로 수행될 커스텀한 기능들을 선언할때 사용
//                    throw new IllegalArgumentException("FAILED..!!");
                    log.info(">>> This is a step 1.");
                    log.info(">>> requestDate = {}", requestDate);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    public Step simpleStep2(@Value("#{jobParameters[requestDate]}") String requestDate) {

        return stepBuilderFactory.get("simpleStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>> This is a step 2.");
                    log.info(">>> requestDate = {}", requestDate);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
