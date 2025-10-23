package sura.pruebalegoback.usecase.todo;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import sura.pruebalegoback.domain.common.StringUtils;
import sura.pruebalegoback.domain.todo.TaskToDo;
import sura.pruebalegoback.domain.todo.gateway.TaskToDoRepository;
import sura.pruebalegoback.domain.user.User;
import sura.pruebalegoback.domain.user.gateway.UserGateway;

import java.time.Duration;

import static reactor.core.publisher.Flux.defer;
import static reactor.core.publisher.Mono.just;
import static reactor.core.publisher.Mono.zip;

@RequiredArgsConstructor
public class QueryTasksUseCase {


    private final TaskToDoRepository tasks;
    private final UserGateway usersGateway;

    //Caché de 30 segundos para optimizar las consultas a la lista de todas las tareas (Opcional)
    private final Flux<TaskToDo> allTasks = defer(this::doFindAll).cache(Duration.ofSeconds(30));

    public Flux<TaskToDo> findAll() {
        return allTasks;
    }

    public Mono<Tuple2<TaskToDo, User>> findTodoWithDetails(String id) {
        return tasks.findById(id).flatMap(task ->
            !StringUtils.isEmpty(task.getAssignedUserId()) ?
                zip(just(task), usersGateway.findById(task.getAssignedUserId())) :
                zip(just(task), just(User.builder().build())));
    }

    private Flux<TaskToDo> doFindAll() {
        return tasks.findAll();
    }
}
