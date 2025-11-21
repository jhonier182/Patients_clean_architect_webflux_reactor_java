package sura.pruebalegoback.domain.todo.events;

import org.junit.jupiter.api.Test;
import sura.pruebalegoback.domain.todo.TaskToDo;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskCreatedTest {

    @Test
    public void shouldHasName() {
        final TaskCreated taskCreated = new TaskCreated(TaskToDo.builder().build(), new Date());
        assertThat(taskCreated.name()).isEqualTo(TaskCreated.EVENT_NAME);
    }
}