package sura.pruebalegoback.domain.user.gateway;

import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.user.User;

public interface UserGateway {
    Mono<User> findById(String id);
}
