package sura.pruebalegoback.usecase.todo;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.common.EventsGateway;
import sura.pruebalegoback.domain.todo.TaskToDo;
import sura.pruebalegoback.domain.todo.TaskToDoFactory;
import sura.pruebalegoback.domain.todo.events.TaskCreated;
import sura.pruebalegoback.domain.todo.gateway.TaskToDoRepository;

import static sura.pruebalegoback.domain.common.UniqueIDGenerator.now;
import static sura.pruebalegoback.domain.common.UniqueIDGenerator.uuid;

@RequiredArgsConstructor
public class CreateTasksUseCase {

    private final TaskToDoRepository tasks;
    private final EventsGateway eventBus;

    public Mono<TaskToDo> createNew(String name, String description) {
        return uuid()
            .flatMap(id -> TaskToDoFactory.createTask(id, name, description))
            .flatMap(tasks::save)
            .flatMap(task -> emitCreatedEvent(task).thenReturn(task));
    }

    private Mono<Void> emitCreatedEvent(TaskToDo task) {
        return now().flatMap(now -> eventBus.emit(new TaskCreated(task, now)));
    }

}
