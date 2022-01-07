package io.confluent.sigmarules.models;

import java.util.List;
import java.util.Map;

public class SigmaDetections {
    private Map<String, List<String>> detections;

    public Map<String, List<String>> getDetections() {
        return detections;
    }

    public void setDetections(Map<String, List<String>> detections) {
        this.detections = detections;
    }
}
