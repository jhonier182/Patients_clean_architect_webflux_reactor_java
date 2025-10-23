package sura.pruebalegoback.defaults;

import lombok.extern.java.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.common.EventsGateway;
import sura.pruebalegoback.domain.todo.TaskToDo;
import sura.pruebalegoback.domain.todo.gateway.TaskToDoRepository;
import sura.pruebalegoback.domain.user.User;
import sura.pruebalegoback.domain.user.gateway.UserGateway;
import sura.pruebalegoback.domain.user.gateway.UserScoreGateway;

import java.util.logging.Level;


/*
 * Contiene definiciones de beans usados por defecto cuando no se han encontrado alternativas de implementación.
 * Se usa para facilitar la demostración de capacidades agregadas gradualmente mediante los diferentes driven-adapters
 */
@Log
@Configuration
public class DefaultBeansConfig {

    @Bean
    @ConditionalOnMissingBean
    public TaskToDoRepository taskToDoRepository() {
        alertFakeBean("TaskToDoRepository");
        return taskToDoRepository;
    }

    @Bean
    @ConditionalOnMissingBean()
    public EventsGateway eventsGateway() {
        alertFakeBean("EventsGateway");
        return eventsGateway;
    }

    @Bean
    @ConditionalOnMissingBean
    public UserGateway userGateway() {
        alertFakeBean("UserGateway");
        return userGateway;
    }

    @Bean
    @ConditionalOnMissingBean
    public UserScoreGateway userScoreGateway() {
        alertFakeBean("UserScoreGateway");
        return userScoreGateway;
    }

    private void alertFakeBean(String beanName) {
        log.log(Level.WARNING, "CONFIGURACION FAKE: " + beanName, beanName);
    }

    private final UserScoreGateway userScoreGateway = (userId, points) -> {
        log.info("Usando UserScoreGateway sin implementación: " +userId + " - " + points);
        return Mono.empty();
    };

    private final UserGateway userGateway = id -> {
        log.info("Usando UserGateway sin implementación");
        return Mono.just(User.builder().id(id).name("User from InMemory Gateway").build());
    };

    private final EventsGateway eventsGateway = event -> {
        log.info("Evento de dominio emitido sólo a sysout:");
        log.info(event.toString());
        return Mono.empty();
    };

    private final TaskToDoRepository taskToDoRepository = new TaskToDoRepository() {
        @Override
        public Mono<TaskToDo> save(TaskToDo taskToDo) {
            log.info("Guardado a repo sin implementación: ");
            log.info(taskToDo.toString());
            return Mono.just(taskToDo);
        }

        @Override
        public Mono<Void> saveAll(Flux<TaskToDo> tasks) {
            log.info("Guardado a repo sin implementación: saveAll");
            return Mono.empty();
        }

        @Override
        public Mono<TaskToDo> findById(String id) {
            log.info("Usando TaskToDoRepository.findById sin implementación");
            return Mono.just(TaskToDo.builder().id(id).name("Task from InMemory Repo").build());
        }

        @Override
        public Flux<TaskToDo> findAll() {
            log.info("Usando TaskToDoRepository.findAll sin implementación");
            return Flux.empty();
        }

        @Override
        public Flux<TaskToDo> findAllUserOpenTasks(String userId) {
            log.info("Usando TaskToDoRepository.findAllUserOpenTasks sin implementación");
            return Flux.empty();
        }
    };

}

