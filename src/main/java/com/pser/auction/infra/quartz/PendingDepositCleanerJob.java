package com.pser.auction.infra.quartz;

import com.pser.auction.application.DepositService;
import com.pser.auction.domain.DepositStatusEnum;
import com.pser.auction.dto.DepositResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingDepositCleanerJob extends QuartzJobBean {
    private final DepositService depositService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String merchantUid = (String) jobDataMap.get("merchantUid");

        DepositResponse depositResponse = depositService.getByMerchantUid(merchantUid);
        DepositStatusEnum status = depositResponse.getStatus();
        if (status.equals(DepositStatusEnum.CREATED) || status.equals(DepositStatusEnum.PAYMENT_VALIDATION_REQUIRED)) {
            depositService.updateToRefundRequired(merchantUid);
        }
    }
}
