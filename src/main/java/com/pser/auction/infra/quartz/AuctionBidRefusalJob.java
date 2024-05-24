package com.pser.auction.infra.quartz;

import com.pser.auction.application.AuctionService;
import com.pser.auction.domain.AuctionStatusEnum;
import com.pser.auction.dto.AuctionDto;
import com.pser.auction.dto.StatusUpdateDto;
import com.pser.auction.exception.StatusUpdateException;
import com.pser.auction.infra.kafka.producer.AuctionStatusProducer;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionBidRefusalJob extends QuartzJobBean {
    private final AuctionService auctionService;
    private final AuctionStatusProducer auctionStatusProducer;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        long auctionId = jobDataMap.getLong("auctionId");

        Try.run(() -> {
                    StatusUpdateDto<AuctionStatusEnum> statusUpdateDto = StatusUpdateDto.<AuctionStatusEnum>builder()
                            .id(auctionId)
                            .targetStatus(AuctionStatusEnum.BID_REFUSAL)
                            .build();
                    auctionService.updateStatus(statusUpdateDto);
                })
                .onSuccess(unused -> {
                    AuctionDto auctionDto = auctionService.getById(auctionId);
                    auctionStatusProducer.produceBidRefusal(auctionDto);
                })
                .recover(StatusUpdateException.class, e -> null)
                .get();
    }
}
