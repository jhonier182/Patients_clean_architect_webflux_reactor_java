package sura.pruebalegoback.infraestructure;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PatientReactiveRepository extends ReactiveCrudRepository<PatientEntity, String> {
    
    @Query("SELECT * FROM patients WHERE active = :active")
    Flux<PatientEntity> findByActive(Boolean active);
    
    @Query("SELECT * FROM patients WHERE document_number = :documentNumber")
    Flux<PatientEntity> findByDocumentNumber(String documentNumber);
    
    @Query("SELECT * FROM patients WHERE city = :city")
    Flux<PatientEntity> findByCity(String city);
    
    @Query("DELETE FROM patients WHERE id = :id")
    Mono<Void> deleteById(String id);
}
