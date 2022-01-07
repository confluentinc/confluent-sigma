package io.confluent.sigmarules.models;

import java.util.ArrayList;
import java.util.List;

public class SigmaRulePredicateList {
    private List<SigmaRulePredicate> predicates = new ArrayList<>();

    public List<SigmaRulePredicate> getPredicates() {
        return predicates;
    }

    public void setDetections(List<SigmaRulePredicate> predicates) {
        this.predicates = predicates;
    }

    public void addDetection(SigmaRulePredicate predicate) {
        this.predicates.add(predicate);
    }

}
