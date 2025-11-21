package sura.pruebalegoback.test;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import sura.pruebalegoback.UseCaseConfig;
import sura.pruebalegoback.defaults.DefaultBeansConfig;
import sura.pruebalegoback.usecase.todo.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UseCaseConfigTest {

    @Autowired
    private CreateTasksUseCase createTasksUseCase;

    @Test
    public void createTasksUseCase() {
        assertThat(createTasksUseCase).isNotNull();
    }

    @Autowired
    private AssignTasksUseCase assignTasksUseCase;

    @Test
    public void assignTasksUseCase() {
        assertThat(assignTasksUseCase).isNotNull();
    }

    @Autowired
    private CompleteTasksUseCase completeTasksUseCase;

    @Test
    public void completeTasksUseCase() {
        assertThat(completeTasksUseCase).isNotNull();
    }

    @Autowired
    private ReAssignUserTasksUseCase reAssignUserTasksUseCase;

    @Test
    public void reAssignUserTasksUseCase() {
        assertThat(reAssignUserTasksUseCase).isNotNull();
    }

    @Autowired
    private QueryTasksUseCase queryTasksUseCase;

    @Test
    public void queryTasksUseCase() {
        assertThat(queryTasksUseCase).isNotNull();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void objectMapper() {
        assertThat(objectMapper).isNotNull();
    }

    @SpringBootApplication
    @Import({UseCaseConfig.class, DefaultBeansConfig.class})
    static class App {
        public static void main(String[] args) {
            SpringApplication.run(App.class, args);
        }
    }

}