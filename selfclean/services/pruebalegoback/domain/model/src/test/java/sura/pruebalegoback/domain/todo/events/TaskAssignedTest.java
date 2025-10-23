package sura.pruebalegoback.domain.todo.events;

import org.junit.jupiter.api.Test;
import sura.pruebalegoback.domain.todo.TaskToDo;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskAssignedTest {

    @Test
    public void shouldHasName() {
        final TaskAssigned taskAssigned = new TaskAssigned(TaskToDo.builder().build(), new Date());
        assertThat(taskAssigned.name()).isEqualTo(TaskAssigned.EVENT_NAME);
    }
}