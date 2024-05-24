package com.pser.auction.domain.event;

import com.pser.auction.domain.BaseEntity;
import com.pser.auction.exception.SameStatusException;
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
        if (status == null) {
            updateStatus();
            return;
        }
        if (status.equals(this.status)) {
            throw new SameStatusException();
        }
        List<StatusEnum> candidates = getStatus().getNext();
        if (candidates == null || !candidates.contains(status)) {
            throw new StatusUpdateException();
        }
        setStatus(status);
    }

    @SuppressWarnings("unchecked")
    public void updateStatus() {
        List<StatusEnum> candidates = getStatus().getNext();
        if (candidates == null || candidates.size() > 1) {
            throw new StatusUpdateException();
        }
        T nextStatus = (T) candidates.get(0);
        setStatus(nextStatus);
    }

    public void rollbackStatusTo(T status) {
        if (status == null) {
            throw new StatusUpdateException();
        }
        if (status.equals(this.status)) {
            throw new SameStatusException();
        }
        T currentStatus = getStatus();
        List<StatusEnum> candidates = status.getNext();
        if (candidates == null || !candidates.contains(currentStatus)) {
            throw new StatusUpdateException();
        }
        setStatus(status);
    }
}