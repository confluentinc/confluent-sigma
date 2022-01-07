package io.confiuent.sigmaui.models;

public class AggregateValues {
    private String groupBy;
    private String distinctValue;
    private String operation;
    private String operationValue;

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getDistinctValue() {
        return distinctValue;
    }

    public void setDistinctValue(String distinctValue) {
        this.distinctValue = distinctValue;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOperationValue() {
        return operationValue;
    }

    public void setOperationValue(String operationValue) {
        this.operationValue = operationValue;
    }
}
