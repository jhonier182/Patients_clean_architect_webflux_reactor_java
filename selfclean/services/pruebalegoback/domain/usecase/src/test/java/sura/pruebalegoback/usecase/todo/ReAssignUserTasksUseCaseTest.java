package sura.pruebalegoback.usecase.todo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import sura.pruebalegoback.domain.todo.TaskToDo;
import sura.pruebalegoback.domain.todo.gateway.TaskToDoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static sura.pruebalegoback.domain.todo.TaskToDo.TaskReportStatus.ASSIGNED;
import static sura.pruebalegoback.domain.todo.TaskToDo.TaskReportStatus.PENDING_REASSIGNMENT;

@ExtendWith(MockitoExtension.class)
public class ReAssignUserTasksUseCaseTest {

    @InjectMocks
    private ReAssignUserTasksUseCase useCase;

    @Mock
    private TaskToDoRepository repository;
    private final String userId = "45";

    private final TaskToDo task1 = TaskToDo.builder().id("1").name("Task 1").description("Task Desc").reportStatus(ASSIGNED).assignedUserId("56").build();
    private final TaskToDo task2 = TaskToDo.builder().id("2").name("Task 2").description("Task Desc").reportStatus(ASSIGNED).assignedUserId("56").build();
    private final TaskToDo task3 = TaskToDo.builder().id("3").name("Task 3").description("Task Desc").reportStatus(ASSIGNED).assignedUserId("56").build();

    private final PublisherProbe<Void> resultMono = PublisherProbe.empty();
    private final ArgumentCaptor<Flux<TaskToDo>> captor = ArgumentCaptor.forClass(Flux.class);

    @BeforeEach
    public void init() {
        final Flux<TaskToDo> tasks = Flux.just(
            task1,
            task2,
            task3
        );
        when(repository.findAllUserOpenTasks(userId)).thenReturn(tasks);
        when(repository.saveAll(any())).thenReturn(resultMono.mono());
    }

    @Test
    public void markUserTaskAsPendingToReAssign() {
        final Mono<Void> result = useCase.markUserTaskAsPendingToReAssign(userId);

        resultMono.assertWasNotSubscribed();
        StepVerifier.create(result).verifyComplete();
        resultMono.assertWasSubscribed();

        verify(repository).saveAll(captor.capture());
        final Flux<TaskToDo> savedFlux = captor.getValue();

        StepVerifier.create(savedFlux)
            .assertNext(task1 ->
                assertThat(task1).extracting(TaskToDo::getAssignedUserId, TaskToDo::getId, TaskToDo::getReportStatus)
                    .containsExactly(null, "1", PENDING_REASSIGNMENT)
            )
            .assertNext(task1 ->
                assertThat(task1).extracting(TaskToDo::getAssignedUserId, TaskToDo::getId, TaskToDo::getReportStatus)
                    .containsExactly(null, "2", PENDING_REASSIGNMENT)
            )
            .assertNext(task1 ->
                assertThat(task1).extracting(TaskToDo::getAssignedUserId, TaskToDo::getId, TaskToDo::getReportStatus)
                    .containsExactly(null, "3", PENDING_REASSIGNMENT)
            ).verifyComplete();
    }
}