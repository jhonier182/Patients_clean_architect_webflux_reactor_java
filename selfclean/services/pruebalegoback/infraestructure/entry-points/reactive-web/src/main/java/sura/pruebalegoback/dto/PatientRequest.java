package sura.pruebalegoback.dto;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {
    
    @NotBlank(message = "El nombre es requerido")
    private String firstName;
    
    @NotBlank(message = "El apellido es requerido")
    private String lastName;
    
    @NotBlank(message = "El número de documento es requerido")
    private String documentNumber;
    
    @NotBlank(message = "El tipo de documento es requerido")
    private String documentType;
    
    @NotNull(message = "La fecha de nacimiento es requerida")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate birthDate;
    
    private String address;
    
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    private String phone;
    
    @NotBlank(message = "La ciudad es requerida")
    private String city;
    
    @NotBlank(message = "El estado es requerido")
    private String state;
}
