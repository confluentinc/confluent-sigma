package io.confluent.sigmarules.models;

public class RuleResults {
    private String title;
    private String id;
    private Boolean isAggregateCondition = false;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Boolean getAggregateCondition() {
        return isAggregateCondition;
    }

    public void setAggregateCondition(Boolean aggregateCondition) {
        isAggregateCondition = aggregateCondition;
    }
}
