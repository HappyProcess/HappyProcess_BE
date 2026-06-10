package com.haapyProcess.domain.member.entity;

/**
 * 강수 비선호 설정값.
 * 질병이 없는 사용자의 '강수 불편도' 점수 산정에 사용한다.
 * NONE: 강수형태와 무관하게 가산점 없음
 * RAIN: 비 성분을 싫어함 (비 계열 코드에 가산점)
 * SNOW: 눈 성분을 싫어함 (눈 계열 코드에 가산점)
 */
public enum PrecipPreference {
    NONE,
    RAIN,
    SNOW
}
