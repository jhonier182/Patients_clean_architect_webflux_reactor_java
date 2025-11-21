package com.sura.reactive.sample;

import lombok.Builder;
import lombok.Data;
import org.reactivecommons.api.domain.Command;
import org.reactivecommons.async.api.AsyncQuery;
import org.reactivecommons.async.api.DirectAsyncGateway;
import org.reactivecommons.async.impl.config.annotations.EnableDirectAsyncGateway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

@SpringBootApplication
@EnableDirectAsyncGateway
public class ConsumeQueryApp {

    static final String TASK_APP = "ServiceExample";

    static final String FIND_WITH_DETAILS = "tasks.task.findWithDetails";

    static final String ASSIGN_TASK_COMMAND = "tasks.task.assign";
    static final String COMPLETE_TASK_COMMAND = "tasks.task.complete";

    public static void main(String[] args) {
        SpringApplication.run(ConsumeQueryApp.class, args);
    }

    @Bean
    public CommandLineRunner runner(DirectAsyncGateway asyncGateway) {
        return args -> {
            final AsyncQuery<?> query = new AsyncQuery<>(FIND_WITH_DETAILS, "c2ba61b4-5ed4-418a-82dd-b94dfe792a67");
            final Mono<TaskWithUser> reply = asyncGateway.requestReply(query, TASK_APP, TaskWithUser.class);
            final TaskWithUser user = reply.block();
            System.out.println(user);

            final Command<?> assignCommand = new Command<>(ASSIGN_TASK_COMMAND, uuid(),
                AssignTaskData.builder().taskId("166ea30b-47ad-44f3-88ed-90957f0f0837").userId("70").build());
            final Mono<Void> result1 = asyncGateway.sendCommand(assignCommand, TASK_APP);
            result1.log().subscribe();


            final Command<?> completeCommand = new Command<>(COMPLETE_TASK_COMMAND, uuid(), "166ea30b-47ad-44f3-88ed-90957f0f0837");
            final Mono<Void> result2 = asyncGateway.sendCommand(completeCommand, TASK_APP);
            result2.log().subscribe();
        };
    }

    private String uuid() {
        return UUID.randomUUID().toString();
    }

}

@Data
class TaskWithUser {
    private TaskToDo task;
    private User user;
}

@Data
class TaskToDo {

    public enum TaskReportStatus {
        PENDING_ASSIGNMENT,
        ASSIGNED,
        PENDING_REASSIGNMENT
    }

    private String id;
    private String name;
    private String description;
    private String assignedUserId;
    private TaskReportStatus reportStatus;
    private Date doneDate;
    private boolean done;

}

@Data
class User {
    private String id;
    private String name;
    private String lastName;
}

@Data
@Builder
class AssignTaskData {
    private final String taskId;
    private final String userId;
}