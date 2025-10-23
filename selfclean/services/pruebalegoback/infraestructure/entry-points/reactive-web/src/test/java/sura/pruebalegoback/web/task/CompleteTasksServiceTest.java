package sura.pruebalegoback.web.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import sura.pruebalegoback.usecase.todo.CompleteTasksUseCase;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.empty;

import java.time.Duration;

@ExtendWith(SpringExtension.class)
@WebFluxTest(CompleteTasksService.class)
public class CompleteTasksServiceTest {

    @Autowired
    private WebTestClient testClient;

    @MockBean
    private CompleteTasksUseCase useCase;
    private final String taskId = "56";

    @Test
    public void shouldCompleteOnlyOneTask() {
        when(useCase.markAsDone(taskId)).thenReturn(empty());

        final ResponseSpec spec = testClient.post()
                .uri("/task/" + taskId + "/complete")
                .exchange();

        spec.expectBody().isEmpty();
        verify(useCase, times(1)).markAsDone(taskId);
    }

    @Test
    public void shouldWorkAsync() {
        when(useCase.markAsDone(taskId)).thenReturn(Mono.delay(Duration.ofSeconds(3))
                .then(Mono.empty()));

        final ResponseSpec spec = testClient.post()
                .uri("/task/" + taskId + "/complete")
                .exchange();

        spec.expectStatus().is2xxSuccessful();
    }

    
}