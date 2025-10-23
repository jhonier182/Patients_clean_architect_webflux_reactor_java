package sura.pruebalegoback.usecase.todo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import sura.pruebalegoback.domain.common.EventsGateway;
import sura.pruebalegoback.domain.todo.TaskToDo;
import sura.pruebalegoback.domain.todo.events.TaskCreated;
import sura.pruebalegoback.domain.todo.gateway.TaskToDoRepository;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateTasksUseCaseTest {

    @InjectMocks
    private CreateTasksUseCase useCase;

    @Mock
    private TaskToDoRepository repository;

    @Mock
    private EventsGateway eventsGateway;

    private ArgumentCaptor<TaskCreated> captor = ArgumentCaptor.forClass(TaskCreated.class);

    private final String desc = "Task Desc..";
    private final String name = "Task Name 1";

    @BeforeEach
    public void init() {
        when(repository.save(any())).then(inv -> Mono.just(inv.getArgument(0)));
        when(eventsGateway.emit(any())).thenReturn(Mono.empty());
    }

    @Test
    public void shouldCreateNew() {
        final Mono<TaskToDo> newtaskMono = useCase.createNew(name, desc);

        //Test no action happens until subscription
        verify(repository, never()).save(any());

        StepVerifier.create(newtaskMono).assertNext(taskToDo -> {
            assertThat(taskToDo).extracting(TaskToDo::getName, TaskToDo::getDescription, TaskToDo::isDone)
                .containsExactly(name, desc, false);
            assertThat(taskToDo.getId()).isNotEmpty();
            assertThat(taskToDo.getAssignedUserId()).isNull();

            verify(eventsGateway).emit(captor.capture());
            final TaskCreated event = captor.getValue();
            Assertions.assertThat(event.getTask()).isEqualTo(taskToDo);
            assertThat(event.getDate()).isCloseTo(new Date(), 1000);

        }).verifyComplete();

        verify(repository, times(1)).save(any());
    }
}