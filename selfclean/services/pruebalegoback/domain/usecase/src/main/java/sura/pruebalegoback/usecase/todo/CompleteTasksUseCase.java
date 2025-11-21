package sura.pruebalegoback.usecase.todo;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import sura.pruebalegoback.domain.common.EventsGateway;
import sura.pruebalegoback.domain.common.ex.BusinessException;
import sura.pruebalegoback.domain.todo.TaskToDo;
import sura.pruebalegoback.domain.todo.TaskToDoOperations;
import sura.pruebalegoback.domain.todo.events.TaskCompleted;
import sura.pruebalegoback.domain.todo.gateway.TaskToDoRepository;
import sura.pruebalegoback.domain.user.gateway.UserScoreGateway;

import java.time.Duration;

import static reactor.core.publisher.Mono.*;
import static sura.pruebalegoback.domain.common.UniqueIDGenerator.now;

@RequiredArgsConstructor
public class CompleteTasksUseCase {

    public static final int COMPLETION_POINTS = 15;
    private final TaskToDoRepository tasks;
    private final EventsGateway eventBus;
    private final UserScoreGateway userScore;

    public Mono<Void> markAsDone(String id){
        return now()
            .flatMap(now -> findById(id)
            .flatMap(TaskToDoOperations.markAsDone(now))) //Aplica la operación de dominio
            .flatMap(tasks::save)
            .flatMap(task -> {
                //Ejemlo: Se paraleliza la ejecución para optimizar tiempo de respuesta
                final Mono<Void> addPointsresult = addCompletionPointsToUser(task);
                final Mono<Void> emitEventResult = emitCompletedEvent(task);
                return when(addPointsresult, emitEventResult); //Realizar las subcripciones de forma paralela
            });
    }

    private Mono<Void> emitCompletedEvent(TaskToDo task) {
        return now().flatMap(now -> eventBus.emit(new TaskCompleted(task, now)));
    }

    private Mono<TaskToDo> findById(String id){
        return tasks.findById(id).switchIfEmpty(error(BusinessException.Type.TASK_NOT_FOUND.defer()));
    }

    private Mono<Void> addCompletionPointsToUser(TaskToDo task) {
        //Ejemplo de acción tentativa, se realiza el mejor esfuerzo por realizarla a nivel de useCase
        // pero se ignora en caso de no poder ejecutarse y se continúa con el flujo normal.
        return userScore.addPointsToUserScore(task.getAssignedUserId(), COMPLETION_POINTS)
            .retryWhen(Retry.backoff(4, Duration.ofMillis(500)))
            .onErrorResume(t -> empty());
    }

}
