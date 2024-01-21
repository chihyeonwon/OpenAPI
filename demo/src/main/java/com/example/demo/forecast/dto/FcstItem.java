package com.example.demo.forecast.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FcstItem {
    // 해변코드
    @JsonProperty("beachNum")
    private int beachNum;

    // 발표일자
    @JsonProperty("baseDate")
    private String baseDate;

    // 발표시각
    @JsonProperty("baseTime")
    private String baseTime;

    // 자료구분코드
    @JsonProperty("category")
    private String category;

    // 예보일자
    @JsonProperty("fcstDate")
    private String fcstDate;

    // 예보시간
    @JsonProperty("fcstTime")
    private String fcstTime;

    // 예보 값
    @JsonProperty("fcstValue")
    private String fcstValue;

    // X좌표
    @JsonProperty("nx")
    private int nx;

    // Y좌표
    @JsonProperty("ny")
    private int ny;

    private String categoryName;
}