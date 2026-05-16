package com.haapyProcess.domain.weather.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class PublicDataResponse<T> {
    private ResponseWrapper<T> response;

    @Getter
    public static class ResponseWrapper<T> {
        private Header header;
        private Body<T> body;
    }

    @Getter
    public static class Header {
        private String resultCode; // "00"이면 정상
        private String resultMsg;
    }

    @Getter
    public static class Body<T> {
        private int numOfRows;
        private int pageNo;
        private int totalCount;
        private Items<T> items; // 진짜 데이터가 들어있는 곳
    }

    @Getter
    public static class Items<T> {
        private List<T> item; // 아이템 배열
    }
}