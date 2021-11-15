package io.confluent.sigmarules.rules;

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.sigmarules.models.SigmaCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ConditionsManager {
    final static Logger logger = LogManager.getLogger(ConditionsManager.class);
    private List<SigmaCondition> conditions;

    public void loadSigmaConditions(String condition) {
    }

    public List<SigmaCondition> getConditions() {
        return this.conditions;
    }

    public Boolean checkConditions(DetectionsManager detections, JsonNode sourceData) {
        for (SigmaCondition c : conditions) {
            if (!c.getAggregateCondition()) {
                if (c.checkCondition(detections, sourceData)) {
                    logger.debug("source data: " + sourceData.toString());
                    return true;
                }
            }
        }

        return false;
    }

    public Boolean hasAggregateConditon() {
        for (SigmaCondition c : conditions) {
            if (c.getAggregateCondition()) {
                return true;
            }
        }

        return false;
    }

    public String getAggregateCondition() {
        for (SigmaCondition c : conditions) {
            if (c.getAggregateCondition()) {
                return c.getConditionName();
            }
        }

        return null;
    }
}
