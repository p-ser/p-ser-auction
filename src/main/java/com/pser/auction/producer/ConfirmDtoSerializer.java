package com.pser.auction.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pser.auction.dto.ConfirmDto;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class ConfirmDtoSerializer implements Serializer<ConfirmDto> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, ConfirmDto data) {
        try {
            if (data == null) {
                return null;
            }
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("직렬화 오류");
        }
    }

    @Override
    public void close() {
    }
}
