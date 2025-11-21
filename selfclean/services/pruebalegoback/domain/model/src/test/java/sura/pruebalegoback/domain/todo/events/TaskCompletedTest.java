package sura.pruebalegoback.domain.todo.events;

import org.junit.jupiter.api.Test;
import sura.pruebalegoback.domain.todo.TaskToDo;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskCompletedTest {

    @Test
    public void shouldHasName() {
        final TaskCompleted taskCompleted = new TaskCompleted(TaskToDo.builder().build(), new Date());
        assertThat(taskCompleted.name()).isEqualTo(TaskCompleted.EVENT_NAME);
    }

}