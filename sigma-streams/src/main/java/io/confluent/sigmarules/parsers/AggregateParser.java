package io.confluent.sigmarules.parsers;

import io.confluent.sigmarules.models.AggregateValues;
import org.apache.commons.lang3.StringUtils;

public class AggregateParser {
    static final public String COUNT = "count";
    static final public String MIN = "min";
    static final public String MAX = "max";
    static final public String AVG = "avg";
    static final public String SUM = "sum";
    static final public String EQUALS = "==";
    static final public String LESS_THAN = "<";
    static final public String LESS_THAN_EQUAL = "<=";
    static final public String GREATER_THAN = ">";
    static final public String GREATER_THAN_EQUAL = ">=";
    static final public String GROUPBY_SEP = "by ";

    //count(distinctValue) by groupBy > 4  distinct value can be null,
    //                                     both can be field name or detection name
    //min
    //max
    //avg
    //sum

    //timeframes
    //    15s  (15 seconds)
    //    30m  (30 minutes)
    //    12h  (12 hours)
    //    7d   (7 days)
    //    3M   (3 months)

    public AggregateValues parseCondition(String condition) {
        String[] countCondition = StringUtils.split(condition);
        //count(distinctValue) [0]
        //by [1]
        //groupBy [2]
        //operation [3]
        //value [4]

        AggregateValues values = new AggregateValues();
        values.setDistinctValue(StringUtils.substringBetween(countCondition[0], "(", ")"));

        if (condition.contains(GROUPBY_SEP)) {
            values.setGroupBy(countCondition[2]);
        }

        if (condition.contains(EQUALS)) {
            values.setOperation(EQUALS);
        } else if (condition.contains(LESS_THAN)) {
            values.setOperation(LESS_THAN);
        } else if (condition.contains(LESS_THAN_EQUAL)) {
            values.setOperation(LESS_THAN_EQUAL);
        } else if (condition.contains(GREATER_THAN)) {
            values.setOperation(GREATER_THAN);
        } else if (condition.contains(GREATER_THAN_EQUAL)) {
            values.setOperation(GREATER_THAN_EQUAL);
        }

        values.setOperationValue(StringUtils.deleteWhitespace(StringUtils.substringAfter(condition,
                values.getOperation())));

        return values;
    }

    public static void main(String[] args) {
        AggregateParser parser = new AggregateParser();
        AggregateValues values = parser.parseCondition("count() > 4");

        System.out.println("Distinct Value: " + values.getDistinctValue());
        System.out.println("GroupBy: " + values.getGroupBy());
        System.out.println("Operation: " + values.getOperation());
        System.out.println("Value: " + values.getOperationValue());



    }
}
