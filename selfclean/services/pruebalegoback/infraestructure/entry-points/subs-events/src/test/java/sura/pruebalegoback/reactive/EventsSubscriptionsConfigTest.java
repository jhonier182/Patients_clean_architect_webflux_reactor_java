package sura.pruebalegoback.reactive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.api.domain.DomainEvent;
import org.reactivecommons.async.api.HandlerRegistry;
import org.reactivecommons.async.api.handlers.registered.RegisteredEventListener;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import sura.pruebalegoback.domain.todo.events.TaskCreated;
import sura.pruebalegoback.usecase.todo.ReAssignUserTasksUseCase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sura.pruebalegoback.reactive.EventsSubscriptionsConfig.TaskCreatedDTO;
import static sura.pruebalegoback.reactive.EventsSubscriptionsConfig.TaskToDoDTO;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class EventsSubscriptionsConfigTest {

    @InjectMocks
    private EventsSubscriptionsConfig config;

    @Mock
    private ReAssignUserTasksUseCase reAssignUseCase;

    private RegisteredEventListener eventListener;

    @BeforeEach
    public void init() {
        final HandlerRegistry registry = config.eventSubscriptions();
        final Stream<RegisteredEventListener<?>> listenerStream = registry.getDomainEventListeners().values().stream()
                .flatMap(List::stream)
                .filter(listener -> listener.getPath().equals(TaskCreated.EVENT_NAME));
        eventListener = listenerStream.findFirst().get();
        assertThat(eventListener.getPath()).isNotBlank();
    }

    @Test
    public void shouldReassignedBeInvoked() {
        final String taskId = "12345";
        when(reAssignUseCase.markUserTaskAsPendingToReAssign(taskId)).thenReturn(Mono.empty());

        TaskToDoDTO task = new TaskToDoDTO();
        task.setId(taskId);
        task.setName("myTask");
        task.setDescription("MyDescription");

        TaskCreatedDTO taskCreated = new TaskCreatedDTO();
        taskCreated.setTask(task);
        taskCreated.setDate(new Date());
        DomainEvent<TaskCreatedDTO> event = new DomainEvent<>(TaskCreated.EVENT_NAME, "", taskCreated);

        Mono<Void> execution = eventListener.getHandler().handle(event);
        StepVerifier.create(execution).verifyComplete();
        verify(reAssignUseCase, times(1)).markUserTaskAsPendingToReAssign(taskId);
    }
}