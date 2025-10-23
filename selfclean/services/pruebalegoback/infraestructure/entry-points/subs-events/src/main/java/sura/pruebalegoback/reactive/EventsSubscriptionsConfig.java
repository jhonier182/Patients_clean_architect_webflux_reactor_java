package sura.pruebalegoback.reactive;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.reactivecommons.async.api.HandlerRegistry;
import org.reactivecommons.async.impl.config.annotations.EnableMessageListeners;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sura.pruebalegoback.domain.todo.TaskToDo;
import sura.pruebalegoback.domain.todo.events.TaskCreated;
import sura.pruebalegoback.usecase.todo.ReAssignUserTasksUseCase;

import java.util.Date;

import static org.reactivecommons.async.api.HandlerRegistry.register;

@Configuration
@EnableMessageListeners
@RequiredArgsConstructor
public class EventsSubscriptionsConfig {

    private final ReAssignUserTasksUseCase useCase;

    @Bean
    public HandlerRegistry eventSubscriptions() {
        return register()
            .listenEvent(TaskCreated.EVENT_NAME, event -> useCase.markUserTaskAsPendingToReAssign(event.getData().getTask().getId()), TaskCreatedDTO.class);
    }

    @Data
    static class TaskCreatedDTO {
        private TaskToDoDTO task;
        private Date date;
    }

    @Data
    static class TaskToDoDTO {
        private String id;
        private String name;
        private String description;
        private TaskToDo.TaskReportStatus reportStatus;
    }

}
