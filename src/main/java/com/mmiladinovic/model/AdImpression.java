package com.mmiladinovic.model;

/**
 * Created by miroslavmiladinovic on 16/08/15.
 */
public class AdImpression {

    private Long appId;
    private String countryCode;
    private Long adId;

    public AdImpression() {
    }

    public AdImpression(Long appId, String countryCode, Long adId) {
        this.appId = appId;
        this.countryCode = countryCode;
        this.adId = adId;
    }

    public Long getAppId() {
        return appId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public Long getAdId() {
        return adId;
    }

    @Override
    public String toString() {
        return "AdImpression{" +
                "appId=" + appId +
                ", countryCode='" + countryCode + '\'' +
                ", adId=" + adId +
                '}';
    }
}
