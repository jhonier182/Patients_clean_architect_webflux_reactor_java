package sura.pruebalegoback.domain.patient.gateway;

import reactor.core.publisher.Mono;

public interface WeatherGateway {

    Mono<WeatherInfo> getWeatherByLocation(String city, String state);

    record WeatherInfo(
            String city,
            String state,
            String temperature,
            String condition,
            String humidity,
            String windSpeed,
            String forecast
    ){}
}
