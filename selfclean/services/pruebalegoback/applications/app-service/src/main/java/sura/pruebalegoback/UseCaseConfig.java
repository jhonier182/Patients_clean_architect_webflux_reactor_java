package sura.pruebalegoback;

import org.reactivecommons.utils.ObjectMapper;
import org.reactivecommons.utils.ObjectMapperImp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sura.pruebalegoback.domain.common.EventsGateway;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;
import sura.pruebalegoback.domain.patient.gateway.WeatherGateway;
import sura.pruebalegoback.domain.todo.gateway.TaskToDoRepository;
import sura.pruebalegoback.domain.user.gateway.UserGateway;
import sura.pruebalegoback.domain.user.gateway.UserScoreGateway;
import sura.pruebalegoback.usecase.patient.*;
import sura.pruebalegoback.usecase.todo.*;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateTasksUseCase createTasksUseCase(TaskToDoRepository tasks, EventsGateway eventGateway) {
        return new CreateTasksUseCase(tasks, eventGateway);
    }

    @Bean
    public AssignTasksUseCase assignTasksUseCase(TaskToDoRepository tasks, UserGateway users, EventsGateway eventsGateway) {
        return new AssignTasksUseCase(tasks, users, eventsGateway);
    }

    @Bean
    public CompleteTasksUseCase completeTasksUseCase(TaskToDoRepository tasks, EventsGateway eventsGateway, UserScoreGateway userScoreGateway) {
        return new CompleteTasksUseCase(tasks, eventsGateway, userScoreGateway);
    }

    @Bean
    public ReAssignUserTasksUseCase reAssignUserTasksUseCase(TaskToDoRepository tasks) {
        return new ReAssignUserTasksUseCase(tasks);
    }

    @Bean
    public QueryTasksUseCase queryTasksUseCase(TaskToDoRepository tasks, UserGateway usersGateway) {
        return new QueryTasksUseCase(tasks, usersGateway);
    }

    @Bean
    public CreatePatientUseCase createPatientUseCase(PatientRepository patientRepository, EventsGateway eventsGateway) {
        return new CreatePatientUseCase(patientRepository, eventsGateway);
    }

    @Bean
    public GetPatientByIdUseCase getPatientByIdUseCase(PatientRepository patientRepository) {
        return new GetPatientByIdUseCase(patientRepository);
    }

    @Bean
    public QueryPatientsUseCase queryPatientsUseCase(PatientRepository patientRepository) {
        return new QueryPatientsUseCase(patientRepository);
    }

    @Bean
    public UpdatePatientUseCase updatePatientUseCase(PatientRepository patientRepository) {
        return new UpdatePatientUseCase(patientRepository);
    }

    @Bean
    public DeletePatientUseCase deletePatientUseCase(PatientRepository patientRepository) {
        return new DeletePatientUseCase(patientRepository);
    }

    @Bean
    public GetPatientWeatherUseCase getPatientWeatherUseCase(PatientRepository patientRepository, WeatherGateway weatherGateway) {
        return new GetPatientWeatherUseCase(patientRepository, weatherGateway);
    }

    @Bean
    public ExportPatientsToExcelUseCase exportPatientsToExcelUseCase(
            PatientRepository patientRepository,
            ExportPatientsToExcelUseCase.ExcelExportService excelExportService) {
        return new ExportPatientsToExcelUseCase(patientRepository, excelExportService);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapperImp();
    }

}
