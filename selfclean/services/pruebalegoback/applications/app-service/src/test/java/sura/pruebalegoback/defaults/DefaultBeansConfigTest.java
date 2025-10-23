package sura.pruebalegoback.defaults;

import static org.assertj.core.api.Assertions.*;


import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import sura.pruebalegoback.domain.todo.TaskToDo;
import sura.pruebalegoback.domain.todo.events.TaskAssigned; 

import java.util.Date;

public class DefaultBeansConfigTest {

    private DefaultBeansConfig config = new DefaultBeansConfig();

    @Test
    public void taskToDoRepositoryShouldHaveNoError() {
        assertThat(config.taskToDoRepository()).isNotNull();
        assertThat(config.taskToDoRepository().findAll()).isNotNull();
        assertThat(config.taskToDoRepository().save(TaskToDo.builder().build())).isNotNull();
        assertThat(config.taskToDoRepository().findById("2")).isNotNull();
        assertThat(config.taskToDoRepository().saveAll(Flux.empty())).isNotNull();
        assertThat(config.taskToDoRepository().findAllUserOpenTasks("3")).isNotNull();
    }

    @Test
    public void eventsGatewayShouldHaveNoError() {
        assertThat(config.eventsGateway()).isNotNull();
        assertThat(config.eventsGateway().emit(new TaskAssigned(TaskToDo.builder().build(), new Date()))).isNotNull();
    }

    @Test
    public void userGateway() {
        assertThat(config.userGateway()).isNotNull();
        assertThat(config.userGateway().findById("2")).isNotNull();
    }

    @Test
    public void userScoreGateway() {
        assertThat(config.userScoreGateway()).isNotNull();
        assertThat(config.userScoreGateway().addPointsToUserScore("1", 12)).isNotNull();
    }
}