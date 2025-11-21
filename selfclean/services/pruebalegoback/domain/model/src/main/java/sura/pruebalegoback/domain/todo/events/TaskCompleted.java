package sura.pruebalegoback.domain.todo.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import sura.pruebalegoback.domain.common.Event;
import sura.pruebalegoback.domain.todo.TaskToDo;

import java.util.Date;

@Data
@RequiredArgsConstructor
public class TaskCompleted implements Event {

    public static final String EVENT_NAME = "todoTasks.task.completed";
    private final TaskToDo task;
    private final Date date;

    @Override
    public String name() {
        return EVENT_NAME;
    }
}
