package io.confiuent.sigmaui.models;

public class RuleResults {
    private String title;

    private String id;

    private Boolean isAggregateCondition = Boolean.valueOf(false);

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getAggregateCondition() {
        return this.isAggregateCondition;
    }

    public void setAggregateCondition(Boolean aggregateCondition) {
        this.isAggregateCondition = aggregateCondition;
    }
}
