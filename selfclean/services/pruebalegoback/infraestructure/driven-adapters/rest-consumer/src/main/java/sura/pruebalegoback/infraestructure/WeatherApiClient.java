package sura.pruebalegoback.infraestructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import sura.pruebalegoback.domain.patient.gateway.WeatherGateway;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

@Component
public class WeatherApiClient implements WeatherGateway {
    
    private static final Logger log = LoggerFactory.getLogger(WeatherApiClient.class);
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${weather.api.base-url}")
    private String baseUrl;
    
    public WeatherApiClient(WebClient webClient, @Qualifier("jacksonObjectMapper") ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<WeatherInfo> getWeatherByLocation(String city, String state) {
        log.info("Consultando clima para: {}, {}", city, state);
        
        // Obtener coordenadas basadas en la ciudad (simplificado - usar coordenadas conocidas)
        double[] coordinates = getCoordinatesForCity(city, state);
        if (coordinates == null) {
            log.warn("No se encontraron coordenadas para {}, {}. Usando datos por defecto", city, state);
            return Mono.just(createDefaultWeather(city, state));
        }
        
        String pointsEndpoint = String.format(Locale.US, "/points/%.4f,%.4f", coordinates[0], coordinates[1]);
        String fullUrl = baseUrl + pointsEndpoint;
        log.info("Consultando endpoint de puntos: {}", fullUrl);
        
        // Paso 1: Obtener metadatos del punto (gridId, gridX, gridY)
        return webClient.get()
                .uri(fullUrl)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                    response -> {
                        log.error("Error HTTP al consultar puntos de clima. Status: {}, URL: {}", response.statusCode(), fullUrl);
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Cuerpo del error 404: {}", errorBody);
                                    return Mono.error(new RuntimeException("Error HTTP: " + response.statusCode() + " - " + errorBody));
                                });
                    })
                .bodyToMono(String.class)
                .flatMap(pointsJson -> {
                    try {
                        PointsResponse pointsResponse = objectMapper.readValue(pointsJson, PointsResponse.class);
                        PointsResponse.PointsProperties props = pointsResponse.getProperties();
                        
                        if (props == null || props.getGridId() == null || props.getGridX() == null || props.getGridY() == null) {
                            log.warn("La respuesta de puntos no contiene información válida. Usando datos por defecto para {}, {}", city, state);
                            return Mono.just(createDefaultWeather(city, state));
                        }
                        
                        // Paso 2: Obtener datos meteorológicos del gridpoint
                        String gridpointEndpoint = String.format("/gridpoints/%s/%d,%d/forecast", 
                                props.getGridId(), props.getGridX(), props.getGridY());
                        String fullGridpointUrl = baseUrl + gridpointEndpoint;
                        log.info("Consultando endpoint de gridpoint: {}", fullGridpointUrl);
                        
                        return webClient.get()
                                .uri(fullGridpointUrl)
                                .retrieve()
                                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                                    response -> {
                                        log.error("Error HTTP al consultar gridpoint. Status: {}, URL: {}", response.statusCode(), fullGridpointUrl);
                                        return response.bodyToMono(String.class)
                                                .flatMap(errorBody -> {
                                                    log.error("Cuerpo del error del gridpoint: {}", errorBody);
                                                    return Mono.error(new RuntimeException("Error HTTP: " + response.statusCode() + " - " + errorBody));
                                                });
                                    })
                                .bodyToMono(String.class)
                                .flatMap(gridpointJson -> {
                                    try {
                                        WeatherResponse weatherResponse = objectMapper.readValue(gridpointJson, WeatherResponse.class);
                                        return Mono.just(toWeatherInfo(weatherResponse, city, state));
                                    } catch (JsonProcessingException e) {
                                        log.error("Error al mapear respuesta JSON de gridpoint: {}", e.getMessage());
                                        return Mono.error(new RuntimeException("Error al procesar datos de clima", e));
                                    }
                                });
                    } catch (JsonProcessingException e) {
                        log.error("Error al mapear respuesta JSON de puntos: {}", e.getMessage());
                        return Mono.error(new RuntimeException("Error al procesar datos de puntos", e));
                    }
                })
                .doOnNext(weather -> log.debug("Clima obtenido para {}, {}: {}", city, state, weather.condition()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isTransientError)
                        .doBeforeRetry(retrySignal -> log.warn("Reintentando consulta de clima. Intento: {}, Error: {}",
                                retrySignal.totalRetries(), retrySignal.failure().getMessage())))
                // Capturar todos los tipos de errores y usar datos por defecto
                .onErrorResume(TimeoutException.class, e -> {
                    log.warn("Timeout al consultar API de clima para {}, {}: {}. Usando datos por defecto", 
                            city, state, e.getMessage());
                    return Mono.just(createDefaultWeather(city, state));
                })
                .onErrorResume(java.net.ConnectException.class, e -> {
                    log.warn("Error de conexión al consultar API de clima para {}, {}: {}. Usando datos por defecto", 
                            city, state, e.getMessage());
                    return Mono.just(createDefaultWeather(city, state));
                })
                .onErrorResume(RuntimeException.class, e -> {
                    log.warn("Error al consultar API de clima para {}, {}: {}. Usando datos por defecto", 
                            city, state, e.getMessage());
                    return Mono.just(createDefaultWeather(city, state));
                })
                // Fallback final para cualquier error no capturado anteriormente
                .onErrorReturn(createDefaultWeather(city, state));
    }
    
    /**
     * Obtiene coordenadas para ciudades conocidas de Estados Unidos.
     * En un caso real, se usaría un servicio de geocodificación.
     */
    private double[] getCoordinatesForCity(String city, String state) {
        String cityStateKey = (city + "," + state).toLowerCase();
        
        // Coordenadas de ciudades principales de EE.UU.
        switch (cityStateKey) {
            case "denver,colorado":
            case "denver,co":
                return new double[]{39.7392, -104.9903};
            case "new york,new york":
            case "new york,ny":
            case "new york city,new york":
            case "new york city,ny":
                return new double[]{40.7128, -74.0060};
            case "los angeles,california":
            case "los angeles,ca":
                return new double[]{34.0522, -118.2437};
            case "chicago,illinois":
            case "chicago,il":
                return new double[]{41.8781, -87.6298};
            case "houston,texas":
            case "houston,tx":
                return new double[]{29.7604, -95.3698};
            case "phoenix,arizona":
            case "phoenix,az":
                return new double[]{33.4484, -112.0740};
            case "philadelphia,pennsylvania":
            case "philadelphia,pa":
                return new double[]{39.9526, -75.1652};
            case "san antonio,texas":
            case "san antonio,tx":
                return new double[]{29.4241, -98.4936};
            case "san diego,california":
            case "san diego,ca":
                return new double[]{32.7157, -117.1611};
            case "dallas,texas":
            case "dallas,tx":
                return new double[]{32.7767, -96.7970};
            default:
                log.debug("No se encontraron coordenadas para: {}, {}", city, state);
                return null;
        }
    }

    private boolean isTransientError(Throwable throwable) {
        return throwable instanceof java.net.ConnectException ||
               throwable instanceof TimeoutException;
    }

    private WeatherInfo toWeatherInfo(WeatherResponse response, String city, String state) {
        WeatherResponse.Properties props = response.getProperties();
        if (props == null) {
            log.warn("La respuesta de la API de clima no contiene propiedades. Usando datos por defecto para {}, {}", city, state);
            return createDefaultWeather(city, state);
        }
        
        // Si tiene períodos (del endpoint /forecast), usar el primer período (condiciones actuales)
        if (props.getPeriods() != null && !props.getPeriods().isEmpty()) {
            WeatherResponse.ForecastPeriod period = props.getPeriods().get(0);
            String temperature = period.getTemperature() != null 
                    ? period.getTemperature() + (period.getTemperatureUnit() != null ? "°" + period.getTemperatureUnit() : "")
                    : "N/A";
            String condition = period.getShortForecast() != null ? period.getShortForecast() : "N/A";
            String humidity = period.getRelativeHumidity() != null 
                    ? period.getRelativeHumidity().getValue() + (period.getRelativeHumidity().getUnitCode() != null ? period.getRelativeHumidity().getUnitCode() : "")
                    : "N/A";
            String windSpeed = period.getWindSpeed() != null ? period.getWindSpeed() : "N/A";
            String forecast = period.getDetailedForecast() != null ? period.getDetailedForecast() : "Información no disponible";
            
            return new WeatherInfo(
                    city,
                    state,
                    temperature,
                    condition,
                    humidity,
                    windSpeed,
                    forecast
            );
        }
        
        // Si tiene datos de observación directos
        String temperature = props.getTemperature() != null 
                ? props.getTemperature().getValue() + (props.getTemperature().getUnitCode() != null ? props.getTemperature().getUnitCode() : "")
                : "N/A";
        String condition = props.getWeather() != null ? props.getWeather().getValue() : "N/A";
        String humidity = props.getRelativeHumidity() != null 
                ? props.getRelativeHumidity().getValue() + (props.getRelativeHumidity().getUnitCode() != null ? props.getRelativeHumidity().getUnitCode() : "")
                : "N/A";
        String windSpeed = props.getWindSpeed() != null 
                ? props.getWindSpeed().getValue() + (props.getWindSpeed().getUnitCode() != null ? props.getWindSpeed().getUnitCode() : "")
                : "N/A";
        String forecast = props.getTextDescription() != null ? props.getTextDescription() : "Información no disponible";
        
        return new WeatherInfo(
                city,
                state,
                temperature,
                condition,
                humidity,
                windSpeed,
                forecast
        );
    }

    private WeatherInfo createDefaultWeather(String city, String state) {
        log.info("Usando datos de clima por defecto para {}, {} debido a error en la API", city, state);
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
}
