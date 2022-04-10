package io.confluent.sigmarules.rules;

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.sigmarules.models.DetectionsManager;
import io.confluent.sigmarules.models.SigmaCondition;
import io.confluent.sigmarules.models.SigmaDetection;
import io.confluent.sigmarules.models.SigmaDetections;
import io.confluent.sigmarules.models.SigmaRule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SigmaRuleCheck {
  final static Logger logger = LogManager.getLogger(SigmaRuleCheck.class);

  public Boolean isValid(SigmaRule rule, JsonNode data) {
    if (rule != null)
      return checkConditions(rule, data);

    logger.error("Invalid Rule");
    return false;
  }

  private Boolean checkConditions(SigmaRule rule, JsonNode sourceData) {
    for (SigmaCondition c : rule.getConditionsManager().getConditions()) {
      if (!c.getAggregateCondition()) {
        if (checkCondition(c, rule.getDetectionsManager(), sourceData)) {
          logger.debug("source data: " + sourceData.toString());
          return true;
        }
      }
    }

    return false;
  }

  private Boolean checkCondition(SigmaCondition condition, DetectionsManager detections, JsonNode sourceData) {
    if (!condition.getAggregateCondition()) {
      Boolean pairedResult = false;
      if (condition.getPairedCondition() != null) {
        pairedResult = checkCondition(condition.getPairedCondition(), detections, sourceData);

        // if paired condition is true and operator is OR, no need to check
        if (pairedResult && condition.getOperator().equals("OR")) {
          return true;
        } else if (condition.getOperator().equals("OR")) {
          return checkParentConditions(condition, detections, sourceData);
        } else { //AND case
          Boolean myResult = checkParentConditions(condition, detections, sourceData);
          return pairedResult && myResult;
        }
      } else {
        return checkParentConditions(condition, detections, sourceData);
      }
    }

    return false;
  }

  private Boolean checkParentConditions(SigmaCondition condition, DetectionsManager detections,
      JsonNode sourceData) {
    // detections grouped together are combined for a status
    Boolean validDetections = false;
    SigmaDetections myDetections = detections.getDetectionsByName(condition.getConditionName());

    if (myDetections != null) {
      for (SigmaDetection d : myDetections.getDetections()) {
        String name = d.getName();
        if (sourceData.has(name)) {
          JsonNode sourceValues = sourceData.get(name);

          if (sourceValues.isArray()) {
            for (final JsonNode sourceValue : sourceValues) {
              if (!checkValue(condition, d, sourceValue.asText())) {
                return false;
              } else {
                validDetections = true;
              }
            }
          } else {
            // this is a string
            String sourceValue = sourceValues.asText();
            if (!checkValue(condition, d, sourceValue)) {
              return false;
            } else {
              validDetections = true;
            }
          }
        } else {
          // does not contain all detection names
          return false;
        }
      }

      if (validDetections) {
        return true;
      }
    } else {
      logger.info("No detections for condition: " + condition.getConditionName() +
          " source: " + sourceData.toString());
    }

    return false;
  }

  private Boolean checkValue(SigmaCondition condition, SigmaDetection model, String sourceValue) {
    if (model.matches(sourceValue, condition.getNotCondition())) {
      return true;
    } else {
      return false;
    }
  }

}
