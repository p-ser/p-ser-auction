package com.pser.auction.domain.event;

import com.pser.auction.domain.BaseEntity;
import com.pser.auction.exception.StatusUpdateException;
import jakarta.persistence.MappedSuperclass;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class StatusHolderEntity<T extends StatusEnum> extends BaseEntity implements StatusHolder<T> {
    private T status;

    @Override
    public void updateStatus(T status) {
        List<StatusEnum> candidates = getStatus().getNext();
        if (candidates == null || !candidates.contains(status)) {
            throw new StatusUpdateException();
        }
        setStatus(status);
    }
}
