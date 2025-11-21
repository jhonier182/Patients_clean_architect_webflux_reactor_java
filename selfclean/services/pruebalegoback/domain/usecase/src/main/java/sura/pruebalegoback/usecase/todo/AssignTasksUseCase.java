package sura.pruebalegoback.usecase.todo;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.common.EventsGateway;
import sura.pruebalegoback.domain.common.ex.BusinessException.Type;
import sura.pruebalegoback.domain.todo.TaskToDo;
import sura.pruebalegoback.domain.todo.TaskToDoOperations;
import sura.pruebalegoback.domain.todo.events.TaskAssigned;
import sura.pruebalegoback.domain.todo.gateway.TaskToDoRepository;
import sura.pruebalegoback.domain.user.User;
import sura.pruebalegoback.domain.user.gateway.UserGateway;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.zip;
import static reactor.function.TupleUtils.function;
import static sura.pruebalegoback.domain.common.UniqueIDGenerator.now;

@RequiredArgsConstructor
public class AssignTasksUseCase {

    private final TaskToDoRepository tasks;
    private final UserGateway users;
    private final EventsGateway eventBus;

    public Mono<Void> assignTask(String taskId, String userId){
        return zip(findTask(taskId), findUser(userId))
            .flatMap(function(TaskToDoOperations::assignToUser))
            .flatMap(tasks::save)
            .flatMap(this::emitAssignedEvent);
    }

    private Mono<Void> emitAssignedEvent(TaskToDo task) {
        return now().flatMap(now -> eventBus.emit(new TaskAssigned(task, now)));
    }

    private Mono<TaskToDo> findTask(String id){
        return tasks.findById(id).switchIfEmpty(error(Type.TASK_NOT_FOUND.defer()));
    }

    private Mono<User> findUser(String id){
        return users.findById(id).switchIfEmpty(error(Type.USER_NOT_EXIST.defer()));
    }
}
