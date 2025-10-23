package sura.pruebalegoback.infraestructure;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("patients")
public class PatientEntity {
    
    @Id
    private String id;
    
    @Column("first_name")
    private String firstName;
    
    @Column("last_name")
    private String lastName;
    
    @Column("document_number")
    private String documentNumber;
    
    @Column("document_type")
    private String documentType;
    
    @Column("birth_date")
    private LocalDate birthDate;
    
    @Column("address")
    private String address;
    
    @Column("phone")
    private String phone;
    
    @Column("email")
    private String email;
    
    @Column("city")
    private String city;
    
    @Column("state")
    private String state;
    
    @Column("admission_date")
    private LocalDateTime admissionDate;
    
    @Column("active")
    private Boolean active;
}