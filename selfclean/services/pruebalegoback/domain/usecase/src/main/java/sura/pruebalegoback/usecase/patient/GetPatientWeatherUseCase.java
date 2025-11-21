package sura.pruebalegoback.usecase.patient;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.ex.PatientBusinessException;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;
import sura.pruebalegoback.domain.patient.gateway.WeatherGateway;
import sura.pruebalegoback.domain.patient.gateway.WeatherGateway.WeatherInfo;

@RequiredArgsConstructor
public class GetPatientWeatherUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPatientWeatherUseCase.class);
    private final PatientRepository patientRepository;
    private final WeatherGateway weatherGateway;

    public Mono<PatientWithWeather> execute(String patientId) {
        log.info("Obteniendo datos de paciente con clima para ID: {}", patientId);

        return patientRepository.findById(patientId)
                .switchIfEmpty(Mono.error(PatientBusinessException.Type.PATIENT_NOT_FOUND.build()))
                .flatMap(patient ->
                        Mono.zip(
                                Mono.just(patient),
                                getWeatherWithFallback(patient.getCity(), patient.getState())
                        )
                )
                .map(tuple -> {
                    Patient patient = tuple.getT1();
                    WeatherInfo weather = tuple.getT2();
                    log.info("Datos combinados obtenidos para paciente: {}", patientId);
                    return new PatientWithWeather(patient, weather);
                })
                .doOnError(error -> log.error("Error al obtener datos combinados: {}", error.getMessage()));
    }

    private Mono<WeatherInfo> getWeatherWithFallback(String city, String state) {
        // El WeatherGateway ya maneja todos los errores y devuelve datos por defecto,
        // por lo que este método solo necesita pasar el resultado directamente
        return weatherGateway.getWeatherByLocation(city, state)
                .doOnNext(weather -> log.debug("Clima obtenido para {}, {}: {}",
                        city, state, weather.condition()))
                // Fallback adicional por si acaso el gateway no maneja algún error
                .onErrorResume(error -> {
                    log.warn("Error inesperado al obtener clima para {}, {}: {}. Usando datos por defecto",
                            city, state, error.getMessage());
                    return Mono.just(createDefaultWeather(city, state));
                })
                .onErrorReturn(createDefaultWeather(city, state));
    }

    private WeatherInfo createDefaultWeather(String city, String state) {
        return new WeatherInfo(
                city,
                state,
                "N/A",
                "No disponible",
                "N/A",
                "N/A",
                "Información no disponible (Error)"
        );
    }

    public record PatientWithWeather(
            Patient patient,
            WeatherInfo weather
    ) {}
}