package io.confluent.sigmarules.models;

import java.util.HashMap;
import java.util.Map;

public enum OperatorType {

    BEGINS_WITH("beginswith"),
    STARTS_WITH("startswith"),
    ENDS_WITH("endswith"),
    CONTAINS("contains"),
    REGEX("re"),
    GREATER_THAN("greater_than"),
    LESS_THAN("less_than");

    private final String value;


    OperatorType(String val) {
        value = val;

    }
    private static final Map<String,OperatorType> lookup = new HashMap<String,OperatorType>();

    //Populate the lookup table on loading time
    static
    {
        for(OperatorType t: OperatorType.values())
        {
            lookup.put(t.value, t);
        }
    }

    public static OperatorType getEnum(String val)
    {
        return lookup.get(val);
    }
}
