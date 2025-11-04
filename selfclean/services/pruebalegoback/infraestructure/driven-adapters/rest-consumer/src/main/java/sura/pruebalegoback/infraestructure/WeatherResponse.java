package sura.pruebalegoback.infraestructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    
    @JsonProperty("properties")
    private Properties properties;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Properties {
        // Para el endpoint /forecast - contiene períodos de pronóstico
        @JsonProperty("periods")
        private List<ForecastPeriod> periods;
        
        // Para el endpoint /observations - datos de observación actual
        @JsonProperty("temperature")
        private ValueUnit temperature;
        
        @JsonProperty("relativeHumidity")
        private ValueUnit relativeHumidity;
        
        @JsonProperty("windSpeed")
        private ValueUnit windSpeed;
        
        @JsonProperty("weather")
        private WeatherCondition weather;
        
        @JsonProperty("textDescription")
        private String textDescription;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastPeriod {
        @JsonProperty("temperature")
        private Integer temperature;
        
        @JsonProperty("temperatureUnit")
        private String temperatureUnit;
        
        @JsonProperty("shortForecast")
        private String shortForecast;
        
        @JsonProperty("detailedForecast")
        private String detailedForecast;
        
        @JsonProperty("relativeHumidity")
        private ValueUnit relativeHumidity;
        
        @JsonProperty("windSpeed")
        private String windSpeed;
        
        @JsonProperty("windDirection")
        private String windDirection;
        
        @JsonProperty("isDaytime")
        private Boolean isDaytime;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueUnit {
        @JsonProperty("value")
        private Double value;
        
        @JsonProperty("unitCode")
        private String unitCode;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherCondition {
        @JsonProperty("value")
        private String value;
    }
}
