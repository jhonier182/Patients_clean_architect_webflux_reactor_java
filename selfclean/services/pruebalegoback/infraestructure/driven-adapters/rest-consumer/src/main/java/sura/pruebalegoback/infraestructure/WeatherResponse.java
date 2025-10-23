package sura.pruebalegoback.infraestructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
