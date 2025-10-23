package sura.pruebalegoback.usecase.patient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryPatientsUseCaseTest {

    @Mock
    private PatientRepository patientRepository;

    private QueryPatientsUseCase queryPatientsUseCase;

    @BeforeEach
    void setUp() {
        queryPatientsUseCase = new QueryPatientsUseCase(patientRepository);
    }

    @Test
    void shouldReturnAllPatients() {
        // Given
        Patient patient1 = createTestPatient("1", "John", "Doe");
        Patient patient2 = createTestPatient("2", "Jane", "Smith");
        
        when(patientRepository.findAll()).thenReturn(Flux.just(patient1, patient2));

        // When
        var result = queryPatientsUseCase.getAllPatients();

        // Then
        StepVerifier.create(result)
                .assertNext(patient -> {
                    assertEquals("1", patient.getId());
                    assertEquals("John", patient.getFirstName());
                    assertEquals("Doe", patient.getLastName());
                })
                .assertNext(patient -> {
                    assertEquals("2", patient.getId());
                    assertEquals("Jane", patient.getFirstName());
                    assertEquals("Smith", patient.getLastName());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnActivePatients() {
        // Given
        Patient activePatient = createTestPatient("1", "John", "Doe");
        activePatient = activePatient.toBuilder().active(true).build();
        
        when(patientRepository.findByActive(true)).thenReturn(Flux.just(activePatient));

        // When
        var result = queryPatientsUseCase.getActivePatients();

        // Then
        StepVerifier.create(result)
                .assertNext(patient -> {
                    assertEquals("1", patient.getId());
                    assertEquals("John", patient.getFirstName());
                    assertEquals("Doe", patient.getLastName());
                    assertTrue(patient.isActive());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnPatientsByCity() {
        // Given
        String city = "Bogotá";
        Patient patient1 = createTestPatient("1", "John", "Doe");
        patient1 = patient1.toBuilder().city(city).build();
        Patient patient2 = createTestPatient("2", "Jane", "Smith");
        patient2 = patient2.toBuilder().city(city).build();
        
        when(patientRepository.findByCity(city)).thenReturn(Flux.just(patient1, patient2));

        // When
        var result = queryPatientsUseCase.getPatientsByCity(city);

        // Then
        StepVerifier.create(result)
                .assertNext(patient -> {
                    assertEquals("1", patient.getId());
                    assertEquals("John", patient.getFirstName());
                    assertEquals("Doe", patient.getLastName());
                    assertEquals(city, patient.getCity());
                })
                .assertNext(patient -> {
                    assertEquals("2", patient.getId());
                    assertEquals("Jane", patient.getFirstName());
                    assertEquals("Smith", patient.getLastName());
                    assertEquals(city, patient.getCity());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnPatientsByDocumentNumber() {
        // Given
        String documentNumber = "12345678";
        Patient patient = createTestPatient("1", "John", "Doe");
        patient = patient.toBuilder().documentNumber(documentNumber).build();
        
        when(patientRepository.findByDocumentNumber(documentNumber)).thenReturn(Flux.just(patient));

        // When
        var result = queryPatientsUseCase.getPatientsByDocumentNumber(documentNumber);

        // Then
        StepVerifier.create(result)
                .assertNext(p -> {
                    assertEquals("1", p.getId());
                    assertEquals("John", p.getFirstName());
                    assertEquals("Doe", p.getLastName());
                    assertEquals(documentNumber, p.getDocumentNumber());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnPatientsByAgeRange() {
        // Given
        Patient youngPatient = createTestPatient("1", "John", "Doe");
        youngPatient = youngPatient.toBuilder().birthDate(LocalDate.of(2000, 1, 1)).build();
        
        Patient oldPatient = createTestPatient("2", "Jane", "Smith");
        oldPatient = oldPatient.toBuilder().birthDate(LocalDate.of(1980, 1, 1)).build();
        
        when(patientRepository.findAll()).thenReturn(Flux.just(youngPatient, oldPatient));

        // When
        var result = queryPatientsUseCase.getPatientsWithAge(20, 30);

        // Then
        StepVerifier.create(result)
                .assertNext(summary -> {
                    assertEquals("1", summary.id());
                    assertEquals("John Doe", summary.fullName());
                    assertTrue(summary.age() >= 20 && summary.age() <= 30);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnPatientsByMultipleCities() {
        // Given
        List<String> cities = List.of("Bogotá", "Medellín");
        Patient patient1 = createTestPatient("1", "John", "Doe");
        patient1 = patient1.toBuilder().city("Bogotá").build();
        Patient patient2 = createTestPatient("2", "Jane", "Smith");
        patient2 = patient2.toBuilder().city("Medellín").build();
        
        when(patientRepository.findByCity("Bogotá")).thenReturn(Flux.just(patient1));
        when(patientRepository.findByCity("Medellín")).thenReturn(Flux.just(patient2));

        // When
        var result = queryPatientsUseCase.getPatientsByMultipleCities(cities);

        // Then
        StepVerifier.create(result)
                .assertNext(patients -> {
                    assertEquals(2, patients.size());
                    assertTrue(patients.stream().anyMatch(p -> p.getCity().equals("Bogotá")));
                    assertTrue(patients.stream().anyMatch(p -> p.getCity().equals("Medellín")));
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleRepositoryError() {
        // Given
        when(patientRepository.findAll()).thenReturn(Flux.error(new RuntimeException("Database error")));

        // When
        var result = queryPatientsUseCase.getAllPatients();

        // Then
        StepVerifier.create(result)
                .verifyComplete(); // Should return empty flux on error
    }

    private Patient createTestPatient(String id, String firstName, String lastName) {
        return Patient.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .documentNumber("12345678")
                .documentType("CC")
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .phone("+1234567890")
                .email("test@example.com")
                .city("Bogotá")
                .state("Cundinamarca")
                .admissionDate(LocalDateTime.now())
                .active(true)
                .build();
    }
}
