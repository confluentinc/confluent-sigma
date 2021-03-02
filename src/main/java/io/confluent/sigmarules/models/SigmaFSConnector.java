package io.confluent.sigmarules.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SigmaFSConnector {
    private SigmaRule payload;

    public SigmaRule getPayload() {
        return payload;
    }

    public void setPayload(SigmaRule payload) {
        this.payload = payload;
    }
}
