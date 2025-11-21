package sura.pruebalegoback.domain.patient;


import lombok.Builder;
import lombok.Data;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class Patient {

     private final String id;
     private final String firstName;
     private final String lastName;
     private final String documentNumber;
     private final String documentType;
     private final LocalDate birthDate;
     private final String address;
     private final String phone;
     private final String email;
     private final String city;
     private final String state;
     private final LocalDateTime admissionDate;
     private final boolean active;


   public String getFullName(){
       return firstName + " " + lastName;
   }

   public int getAge(){
       return LocalDate.now().getYear() - birthDate.getYear();
   }


}
