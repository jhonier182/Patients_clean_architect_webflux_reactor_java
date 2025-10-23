package sura.pruebalegoback.web.task;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.usecase.todo.AssignTasksUseCase;

@RestController
@RequiredArgsConstructor
public class AssignTasksService {

    private final AssignTasksUseCase useCase;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/task/assign")
    public Mono<Void> assignTask(@RequestBody AssignTaskData data) {
        return useCase.assignTask(data.getTaskId(), data.getUserId());
    }

    @Data
    private static class AssignTaskData {
        private String taskId;
        private String userId;
    }
}
