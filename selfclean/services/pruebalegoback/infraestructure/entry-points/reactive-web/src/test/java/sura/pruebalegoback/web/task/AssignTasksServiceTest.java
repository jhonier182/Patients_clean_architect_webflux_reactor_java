package sura.pruebalegoback.web.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import lombok.Data;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.usecase.todo.AssignTasksUseCase;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Duration;

@ExtendWith(SpringExtension.class)
@WebFluxTest(AssignTasksService.class)
public class AssignTasksServiceTest {

    @Autowired
    private WebTestClient testClient;

    @MockBean
    private AssignTasksUseCase useCase;
    private static final String taskId = "56";
    private static final String userId = "35";
    AssignTaskData data = new AssignTaskData(taskId, userId);

    private static final String url = "/task/assign";

    @Test
    public void shouldAssignOnlyOneTask() {
        when(useCase.assignTask(taskId, userId)).thenReturn(Mono.empty());

        final ResponseSpec spec = testClient.post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .exchange();

        spec.expectBody().isEmpty();
        verify(useCase, times(1)).assignTask(taskId, userId);
    }

    @Test
    public void shouldWorkAsync() {
        when(useCase.assignTask(taskId, userId)).thenReturn(Mono.delay(Duration.ofSeconds(3))
                .then(Mono.empty()));

        final ResponseSpec spec = testClient.post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .exchange();

        spec.expectStatus().is2xxSuccessful();

    }

    @Data
    private static class AssignTaskData {
        private final String taskId;
        private final String userId;
    }

}