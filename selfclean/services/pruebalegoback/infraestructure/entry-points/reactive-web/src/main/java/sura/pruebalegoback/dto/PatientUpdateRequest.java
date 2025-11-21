package sura.pruebalegoback.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientUpdateRequest {
    
    private String firstName;
    private String lastName;
    private String address;
    
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    private String phone;
    private String city;
    private String state;
}
