package sura.pruebalegoback.usecase.patient;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;

import java.util.List;

@RequiredArgsConstructor
public class ExportPatientsToExcelUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExportPatientsToExcelUseCase.class);
    private final PatientRepository patientRepository;
    private final ExcelExportService excelExportService;

    public Mono<byte[]> exportAllPatients() {
        log.info("Iniciando exportación de pacientes a Excel");

        return patientRepository.findAll()
                .collectList()
                .doOnNext(patients -> log.info("Total de pacientes a exportar: {}", patients.size()))
                .flatMap(this::generateExcel)
                .doOnNext(bytes -> log.info("Excel generado exitosamente. Tamaño: {} bytes", bytes.length))
                .doOnError(error -> log.error("Error al exportar pacientes: {}", error.getMessage()));
    }

    public Mono<byte[]> exportActivePatients() {
        log.info("Iniciando exportación de pacientes activos a Excel");

        return patientRepository.findByActive(true)
                .collectList()
                .flatMap(this::generateExcel)
                .doOnNext(bytes -> log.info("Excel de pacientes activos generado"));
    }

    private Mono<byte[]> generateExcel(List<Patient> patients) {
        return Mono.create(sink -> {
                    try {
                        log.debug("Generando Excel en thread bloqueante: {}", Thread.currentThread().getName());
                        byte[] excelBytes = excelExportService.createExcel(patients);
                        sink.success(excelBytes);
                    } catch (Exception e) {
                        log.error("Error al generar Excel: {}", e.getMessage());
                        sink.error(e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .cast(byte[].class);
    }

    // Interface que implementarás en la capa de infraestructura (helpers)
    public interface ExcelExportService {
        byte[] createExcel(List<Patient> patients) throws Exception;
    }
}