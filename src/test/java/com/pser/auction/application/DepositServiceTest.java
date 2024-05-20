package com.pser.auction.application;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.API.run;
import static io.vavr.Predicates.is;
import static io.vavr.Predicates.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

import com.pser.auction.dao.AuctionDao;
import com.pser.auction.dao.DepositDao;
import com.pser.auction.domain.Auction;
import com.pser.auction.domain.AuctionStatusEnum;
import com.pser.auction.domain.Deposit;
import com.pser.auction.domain.DepositStatusEnum;
import com.pser.auction.dto.DepositCreateRequest;
import com.pser.auction.dto.DepositMapper;
import com.pser.auction.dto.DepositMapperImpl;
import com.pser.auction.infra.kafka.producer.DepositStatusProducer;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@DisplayName("Deposit Service 테스트")
@ExtendWith(MockitoExtension.class)
public class DepositServiceTest {
    @InjectMocks
    DepositService depositService;

    @Spy
    DepositMapper depositMapper = new DepositMapperImpl();

    @Mock
    AuctionDao auctionDao;

    @Mock
    DepositDao depositDao;

    @Mock
    DepositStatusProducer depositStatusProducer;

    DepositCreateRequest request = DepositCreateRequest.builder()
            .auctionId(1L)
            .userId(10L)
            .build();

    Auction auction = Auction.builder()
            .build();

    Deposit deposit = Deposit.builder()
            .auction(auction)
            .userId(request.getUserId())
            .build();


    @Test
    @DisplayName("보증금 생성 또는 결제 진행중인 보증금 가져오기")
    public void getOrSave() {
        Deposit spiedDeposit = spy(deposit);
        auction.setStatus(AuctionStatusEnum.ONGOING);
        given(spiedDeposit.getId()).willReturn(123L);
        given(auctionDao.findById(any())).willReturn(Optional.of(auction));
        given(depositDao.save(any())).willReturn(spiedDeposit);

        depositService.getOrSave(request);

        then(auctionDao).should().findById(any(Long.class));
        then(depositMapper).should().toEntity(any(DepositCreateRequest.class));
        then(depositDao).should().save(any(Deposit.class));
    }

    @Test
    @DisplayName("결제 대기 상태 보증금 체크")
    public void checkStatusGivenCreated() {
        Deposit spiedDeposit = spy(deposit);
        DepositService spiedDepositService = spy(depositService);
        given(spiedDeposit.getStatus()).willReturn(DepositStatusEnum.CREATED);
        willDoNothing().given(spiedDepositService).updateToPaymentValidationRequired(any());
        given(depositDao.findById(any())).willReturn(Optional.of(spiedDeposit));

        spiedDepositService.checkPayment(1L, "");

        then(spiedDepositService).should().updateToPaymentValidationRequired(any());
    }

    @Test
    @DisplayName("결제 대기 상태가 아닌 보증금 체크")
    public void checkStatusGivenNotCreated() {
        Deposit spiedDeposit = spy(deposit);
        DepositService spiedDepositService = spy(depositService);
        given(spiedDeposit.getStatus()).willReturn(DepositStatusEnum.PAYMENT_VALIDATION_REQUIRED);
        given(depositDao.findById(any())).willReturn(Optional.of(spiedDeposit));

        spiedDepositService.checkPayment(1L, "");

        then(spiedDepositService).should(never()).updateToPaymentValidationRequired(any());
    }

    @Test
    public void test() {
//        DepositCreateRequest depositCreateRequest = null;
//        Long value = Optional.ofNullable(depositCreateRequest)
//                .map(DepositCreateRequest::getUserId)
//                .orElse(1L);
        int input = 2;
        String output = Match(input).of(
                Case($(1), "one"),
                Case($(2), "two"),
                Case($(3), "three"),
                Case($(), "?"));
        Match(1).of(
                Case($(not(is(2))), () -> run(() -> log.error("1 isn't 2"))),
                Case($(1), () -> run(() -> log.error("1 is 1"))),
                Case($(3), () -> run(() -> log.error("1 is 3"))),
                Case($(), () -> run(() -> log.error("none")))
        );
        log.error("value=" + output);
    }
}
