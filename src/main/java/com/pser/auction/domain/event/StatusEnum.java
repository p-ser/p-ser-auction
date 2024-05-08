package com.pser.auction.domain.event;

import java.util.List;

public interface StatusEnum {
    List<StatusEnum> getNext();
}
