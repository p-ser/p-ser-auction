package com.pser.auction.dao;

import com.pser.auction.domain.Deposit;
import com.pser.auction.domain.DepositStatusEnum;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface DepositDao extends JpaRepository<Deposit, Long> {
    Optional<Deposit> findByMerchantUid(String merchantUid);

    Optional<Deposit> findByUserIdAndAuctionIdAndStatusIn(Long userId, Long auctionId,
                                                          List<DepositStatusEnum> statusEnums);

    @NonNull
    Page<Deposit> findAllByAuctionId(Long auctionId, @NonNull Pageable pageable);

    List<Deposit> findAllByAuctionIdAndStatusIn(Long auctionId, List<DepositStatusEnum> status);
}
