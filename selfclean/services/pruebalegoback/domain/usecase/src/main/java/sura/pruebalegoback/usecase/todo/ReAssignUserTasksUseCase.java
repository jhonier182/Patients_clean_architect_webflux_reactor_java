package sura.pruebalegoback.usecase.todo;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.todo.gateway.TaskToDoRepository;

import static sura.pruebalegoback.domain.todo.TaskToDoOperations.setPendingToReAssign;

@RequiredArgsConstructor
public class ReAssignUserTasksUseCase {

    private final TaskToDoRepository tasks;

    public Mono<Void> markUserTaskAsPendingToReAssign(String userId) {
        return tasks.findAllUserOpenTasks(userId)
            .map(setPendingToReAssign())
            .as(tasks::saveAll);
    }
}
