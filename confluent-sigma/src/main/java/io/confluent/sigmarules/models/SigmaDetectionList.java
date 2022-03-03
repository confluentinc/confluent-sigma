package io.confluent.sigmarules.models;

import java.util.ArrayList;
import java.util.List;

public class SigmaDetectionList {
    private List<SigmaDetection> detections = new ArrayList<>();

    public List<SigmaDetection> getDetections() {
        return detections;
    }

    public void setDetections(List<SigmaDetection> detections) {
        this.detections = detections;
    }

    public void addDetection(SigmaDetection detection) {
        this.detections.add(detection);
    }
}
