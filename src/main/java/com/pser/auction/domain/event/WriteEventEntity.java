package com.pser.auction.domain.event;

import com.pser.auction.domain.BaseEntity;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MappedSuperclass
public class WriteEventEntity extends BaseEntity {
    @Transient
    private List<Consumer<WriteEventEntity>> onCreatedEventHandlers = new ArrayList<>();

    @Transient
    private List<Consumer<WriteEventEntity>> onUpdatedEventHandlers = new ArrayList<>();

    @Transient
    private List<Consumer<WriteEventEntity>> onDeletedEventHandlers = new ArrayList<>();

    public void addOnCreatedEventHandler(
            Consumer<WriteEventEntity> onCreatedEventHandler) {
        this.onCreatedEventHandlers.add(onCreatedEventHandler);
    }

    public void addOnUpdatedEventHandler(
            Consumer<WriteEventEntity> onUpdatedEventHandler) {
        this.onUpdatedEventHandlers.add(onUpdatedEventHandler);
    }

    public void addOnDeletedEventHandler(
            Consumer<WriteEventEntity> onDeletedEventHandler) {
        this.onDeletedEventHandlers.add(onDeletedEventHandler);
    }

    @PostPersist
    private void onCreated() {
        onCreatedEventHandlers.forEach(handler -> handler.accept(this));
    }

    @PostUpdate
    private void onUpdated() {
        onUpdatedEventHandlers.forEach(handler -> handler.accept(this));
    }

    @PostRemove
    private void onDeleted() {
        onDeletedEventHandlers.forEach(handler -> handler.accept(this));
    }
}
