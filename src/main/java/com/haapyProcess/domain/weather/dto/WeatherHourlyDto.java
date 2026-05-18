package com.haapyProcess.domain.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WeatherHourlyDto {
    private String time;
    private String temperature;
    private String condition;
    private String humidity;

    @JsonIgnore
    private String sky;

    @JsonIgnore
    private String pty;
}