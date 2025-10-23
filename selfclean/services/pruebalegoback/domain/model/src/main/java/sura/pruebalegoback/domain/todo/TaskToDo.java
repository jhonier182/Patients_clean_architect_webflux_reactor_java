package sura.pruebalegoback.domain.todo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder(toBuilder = true)
public class TaskToDo {

    public enum TaskReportStatus {
        PENDING_ASSIGNMENT,
        ASSIGNED,
        PENDING_REASSIGNMENT
    }

    private final String id;
    private final String name;
    private final String description;
    private final String assignedUserId;
    private final TaskReportStatus reportStatus;
    private final Date doneDate;
    private final boolean done;

}
