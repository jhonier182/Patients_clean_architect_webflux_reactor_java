package sura.pruebalegoback.web.task;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import sura.pruebalegoback.domain.todo.TaskToDo;
import sura.pruebalegoback.domain.user.User;
import sura.pruebalegoback.usecase.todo.QueryTasksUseCase;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@WebFluxTest(QueryTaskServices.class)
public class QueryTaskServicesTest {

	@Autowired
	private WebTestClient testClient;

	@MockBean
	private QueryTasksUseCase useCase;
	private static final String taskId = "01";
	private static final TaskToDo task1 = TaskToDo.builder().id(taskId).name("Task 1").description("Task Desc")
		.assignedUserId("03").build();
	private static final TaskToDo task2 = TaskToDo.builder().id("02").name("Task 2").description("Task Desc")
		.assignedUserId("56").build();
	private static final TaskToDo task3 = TaskToDo.builder().id("03").name("Task 3").description("Task Desc")
		.build();
	private static final User user = User.builder().name("Daniel").id("03").lastName("Ospina").build();
	private static final String url = "/task/";
	private static final Tuple2<TaskToDo, User> withDetails = Tuples.of(task1, user);

	@Test
	public void shouldFindOnceSucesfull() {
		when(useCase.findTodoWithDetails(taskId)).thenReturn(Mono.just(withDetails));

		final WebTestClient.ResponseSpec spec = testClient.get().uri(url + taskId)
			.exchange();

		spec.expectStatus().isOk();
		verify(useCase, times(1)).findTodoWithDetails(taskId);
	}

	@Test
	public void shouldFindWithDetails() {
		when(useCase.findTodoWithDetails(taskId)).thenReturn(Mono.just(withDetails));

		final WebTestClient.ResponseSpec spec = testClient.get().uri(url + taskId)
			.exchange();

		spec.expectBody(TaskWithUser.class).consumeWith(res -> {
			final TaskWithUser body = res.getResponseBody();
			assertThat(body).isNotNull();
			assertAll(() -> {
				assertThat(body.getTask().getId()).isEqualTo(taskId);
				assertThat(body.getTask().getName()).isEqualTo("Task 1");
				assertThat(body.getUser().getName()).isEqualTo("Daniel");
			});
		});
	}

	@Test
	public void shouldFindAsync() {
		when(useCase.findTodoWithDetails(taskId)).thenReturn(Mono.delay(Duration.ofSeconds(3))
			.then(Mono.just(withDetails)));

		final WebTestClient.ResponseSpec spec = testClient.get().uri(url + taskId)
			.exchange();

		spec.expectStatus().isOk();

	}

	@Test
	public void shouldListAllTasks() {
		final Flux<TaskToDo> tasks = Flux.just(
			task1,
			task2,
			task3);
		when(useCase.findAll()).thenReturn(tasks);

		final WebTestClient.ResponseSpec spec = testClient.get().uri("/task")
			.exchange();
		spec.expectStatus().isOk()
			.expectBodyList(TaskToDoDTO.class).consumeWith(res -> {
				final List<TaskToDoDTO> body = res.getResponseBody();
				assertThat(body).hasSize(3).extracting(TaskToDoDTO::getName)
						.containsExactly("Task 1", "Task 2", "Task 3");
			});
		verify(useCase, times(1)).findAll();

	}

	@Test
	public void shouldListTasksAsync() {
		final Flux<TaskToDo> tasks = Flux.just(
			task1,
			task2,
			task3).delayElements(Duration.ofSeconds(2));
		when(useCase.findAll()).thenReturn(tasks);

		final WebTestClient.ResponseSpec spec = testClient.get().uri("/task")
			.exchange();

		spec.expectStatus().isOk();

	}

	@Data
	@NoArgsConstructor
	private static class TaskToDoDTO {
		private String id;
		private String name;
		private String description;
		private String assignedUserId;
	}

	@Data
	@NoArgsConstructor
	private static class UserDTO {
		private String id;
		private String name;
		private String lastName;
	}

	@Data
	@NoArgsConstructor
	private static class TaskWithUser {
		private TaskToDoDTO task;
		private UserDTO user;
	}

}