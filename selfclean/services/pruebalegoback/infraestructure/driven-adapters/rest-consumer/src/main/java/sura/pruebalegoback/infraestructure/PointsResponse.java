package sura.pruebalegoback.infraestructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointsResponse {
    
    @JsonProperty("properties")
    private PointsProperties properties;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointsProperties {
        @JsonProperty("gridId")
        private String gridId;
        
        @JsonProperty("gridX")
        private Integer gridX;
        
        @JsonProperty("gridY")
        private Integer gridY;
        
        @JsonProperty("forecast")
        private String forecast;
        
        @JsonProperty("forecastHourly")
        private String forecastHourly;
        
        @JsonProperty("forecastGridData")
        private String forecastGridData;
        
        @JsonProperty("observationStations")
        private String observationStations;
    }
}

