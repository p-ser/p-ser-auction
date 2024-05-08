package com.pser.auction.dao;

import com.pser.auction.domain.Deposit;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositDao extends JpaRepository<Deposit, Long> {
    Optional<Deposit> findByMerchantUid(String merchantUid);
}
