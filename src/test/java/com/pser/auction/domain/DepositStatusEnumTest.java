package com.pser.auction.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("Deposit 엔티티 상태 진행 테스트")
@ExtendWith(MockitoExtension.class)
public class DepositStatusEnumTest {

    @Test
    @DisplayName("정상적으로 다음 상태로 업데이트 하는 경우")
    public void updateStatus() {
        Deposit deposit = Deposit.builder().build();

        deposit.updateStatus(DepositStatusEnum.CONFIRM_AWAITING);

        Assertions.assertThat(deposit.getStatus()).isEqualTo(DepositStatusEnum.CONFIRM_AWAITING);
    }

    @Test
    @DisplayName("진행 불가한 상태로 업데이트 하는 경우")
    public void updateToWrongStatus() {
        Deposit deposit = Deposit.builder().build();

        Throwable throwable = Assertions.catchThrowable(() -> deposit.updateStatus(DepositStatusEnum.CONFIRMED));

        Assertions.assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }
}
