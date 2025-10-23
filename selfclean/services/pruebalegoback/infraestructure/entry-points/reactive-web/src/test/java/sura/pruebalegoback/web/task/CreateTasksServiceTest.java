package sura.pruebalegoback.web.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import sura.pruebalegoback.domain.todo.TaskToDo;
import sura.pruebalegoback.usecase.todo.CreateTasksUseCase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static reactor.core.publisher.Mono.just;
import java.time.Duration;

@ExtendWith(SpringExtension.class)
@WebFluxTest(CreateTasksService.class)
public class CreateTasksServiceTest {

    @Autowired
    private WebTestClient testClient;

    @MockBean
    private CreateTasksUseCase useCase;

    private static final String id = "01";
    private static final String name = "Task 1";
    private final String description = "Desc Test task";
    NewTaskData data = new NewTaskData(name, description);
    private static final String url = "/task";

    @Test
    public void shouldCreateOnlyOneTask() {
        when(useCase.createNew(name, description))
            .then(i -> just(TaskToDo.builder().name(name).description(description).id(id).build()));

        final WebTestClient.ResponseSpec spec = testClient.post().uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(data)
            .exchange();

        spec.expectBody(TaskToDoDTO.class).consumeWith(res -> {
            final TaskToDoDTO body = res.getResponseBody();
            assertThat(body).extracting(TaskToDoDTO::getId, TaskToDoDTO::getName, TaskToDoDTO::getDescription)
                .containsExactly(id, name, description);
        });
        verify(useCase, times(1)).createNew(name, description);

    }

    @Test
    public void shouldWorkAsync() {
        when(useCase.createNew(name, description))
            .then(i -> just(TaskToDo.builder().name(name).description(description).id("01").build())
            .delayElement(Duration.ofSeconds(3)));

        final WebTestClient.ResponseSpec spec = testClient.post().uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(data)
            .exchange();

        spec.expectStatus().is2xxSuccessful();

    }

    @Data
    private static class TaskToDoDTO {
        private String id;
        private String name;
        private String description;
    }

    @Data
    @AllArgsConstructor
    private static class NewTaskData {
        private String name;
        private String description;
    }

}