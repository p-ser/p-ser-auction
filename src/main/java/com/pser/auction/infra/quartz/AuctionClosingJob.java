package com.pser.auction.infra.quartz;

import com.pser.auction.application.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionClosingJob extends QuartzJobBean {
    AuctionService auctionService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        long auctionId = jobDataMap.getLong("auctionId");
        auctionService.closeAuction(auctionId);
    }
}
