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
import sura.pruebalegoback.domain.todo.events.TaskAssigned;
import sura.pruebalegoback.domain.todo.gateway.TaskToDoRepository;
import sura.pruebalegoback.domain.user.User;
import sura.pruebalegoback.domain.user.gateway.UserGateway;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssignTasksUseCaseTest {

    @InjectMocks
    private AssignTasksUseCase useCase;

    @Mock
    private TaskToDoRepository repository;

    @Mock
    private EventsGateway eventsGateway;

    @Mock
    private UserGateway users;

    private ArgumentCaptor<TaskAssigned> captor = ArgumentCaptor.forClass(TaskAssigned.class);
    private final String taskId = "42";
    private final String userId = "56";
    private final TaskToDo taskInDB = taskInDB();
    private final User remoteUser = remoteUser();


    @BeforeEach
    public void init() {
        when(repository.findById(taskId)).thenReturn(Mono.just(taskInDB));
        when(users.findById(userId)).thenReturn(Mono.just(remoteUser));
        when(repository.save(any())).then(inv -> Mono.just(inv.getArgument(0)));
        when(eventsGateway.emit(any())).thenReturn(Mono.empty());
    }

    @Test
    public void assignTask() {
        final Mono<Void> result = useCase.assignTask(taskId, userId);

        //Test no action happens until subscription
        verify(repository, never()).save(any());
        verify(eventsGateway, never()).emit(any());

        StepVerifier.create(result).verifyComplete();

        verify(eventsGateway).emit(captor.capture());
        verify(repository).save(any(TaskToDo.class));
        final TaskAssigned event = captor.getValue();
        Assertions.assertThat(event.getTask()).extracting(TaskToDo::getId, TaskToDo::getName, TaskToDo::getAssignedUserId)
            .containsExactly(taskInDB.getId(), taskInDB.getName(), userId);
    }

    private TaskToDo taskInDB() {
        return TaskToDo.builder().id(taskId).name("TaskName").description("TaskDesc")
            .build();
    }

    private User remoteUser() {
        return User.builder().id(userId).name("Daniel").lastName("Ospina").build();
    }

}