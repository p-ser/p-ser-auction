package com.pser.auction.infra.kafka.consumer;

import com.pser.auction.Util;
import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.infra.quartz.PendingDepositCleanerJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositPaymentAwaitingConsumer {
    private final Scheduler scheduler;

    @RetryableTopic(kafkaTemplate = "stringValueKafkaTemplate", attempts = "5")
    @KafkaListener(topics = KafkaTopics.DEPOSIT_PAYMENT_AWAITING, groupId = "${kafka.consumer-group-id}", containerFactory = "stringValueListenerContainerFactory")
    public void scheduleJob(String merchantUid) throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("merchantUid", merchantUid);

        JobDetail job = JobBuilder.newJob(PendingDepositCleanerJob.class)
                .withIdentity("refund.%s".formatted(merchantUid))
                .setJobData(jobDataMap)
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(Util.afterHours(1))
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}
