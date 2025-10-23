package sura.pruebalegoback.dto;



import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import sura.pruebalegoback.domain.patient.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    
    private String id;
    private String firstName;
    private String lastName;
    private String documentNumber;
    private String documentType;
    private LocalDate birthDate;
    private String address;
    private String phone;
    private String email;
    private String city;
    private String state;
    private LocalDateTime admissionDate;
    private boolean active;
    private String fullName;
    private int age;
    
    public static PatientResponse fromDomain(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .documentNumber(patient.getDocumentNumber())
                .documentType(patient.getDocumentType())
                .birthDate(patient.getBirthDate())
                .address(patient.getAddress())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .city(patient.getCity())
                .state(patient.getState())
                .admissionDate(patient.getAdmissionDate())
                .active(patient.isActive())
                .fullName(patient.getFullName())
                .age(patient.getAge())
                .build();
    }
}
