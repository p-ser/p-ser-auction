package com.pser.auction.infra;

import com.pser.auction.common.response.ApiResponse;
import com.pser.auction.dto.ReservationResponse;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotelClient {
    private final RestTemplate restTemplate;
    private final Environment env;

    @NonNull
    public ReservationResponse getReservationById(long reservationId) {
        String url = env.getProperty("service.hotel.url", "");
        ApiResponse<ReservationResponse> response = restTemplate.exchange(
                RequestEntity.get("%s/reservations/%s".formatted(url, reservationId))
                        .build(),
                new ParameterizedTypeReference<ApiResponse<ReservationResponse>>() {
                }
        ).getBody();
        return Optional.ofNullable(response)
                .map(ApiResponse::getBody)
                .orElseThrow();
    }

    public boolean isReservationOwner(long reservationId, long authId) {
        ReservationResponse reservationResponse = getReservationById(reservationId);
        return authId == reservationResponse.getUserId();
    }
}
