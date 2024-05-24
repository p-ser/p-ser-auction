package com.pser.auction.infra.kafka.consumer;

import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.dto.AuctionDto;
import com.pser.auction.infra.quartz.AuctionBidRefusalJob;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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
public class AuctionPaymentRequiredConsumer {
    private final Scheduler scheduler;

    @RetryableTopic(kafkaTemplate = "auctionDtoValueKafkaTemplate", attempts = "5")
    @KafkaListener(topics = KafkaTopics.AUCTION_PAYMENT_REQUIRED, groupId = "${kafka.consumer-group-id}", containerFactory = "auctionDtoValueListenerContainerFactory")
    public void scheduleJob(AuctionDto auctionDto) throws SchedulerException {
        LocalDateTime auctionEndDateTime = auctionDto.getEndAt();
        LocalDateTime paymentLimitDateTime = auctionEndDateTime.plusHours(12);
        Date paymentLimitDate = Date.from(paymentLimitDateTime.atZone(ZoneId.systemDefault()).toInstant());
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("auctionId", auctionDto.getId());

        JobDetail job = JobBuilder.newJob(AuctionBidRefusalJob.class)
                .withIdentity("auction.bid-refusal.%s".formatted(auctionDto.getId()))
                .setJobData(jobDataMap)
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .startAt(paymentLimitDate)
                .build();

        scheduler.scheduleJob(job, trigger);
    }
}
