/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package io.confluent.sigmarules.models;

import java.util.HashMap;
import java.util.Map;

public enum ModifierType {

    BEGINS_WITH("beginswith"),
    STARTS_WITH("startswith"),
    ENDS_WITH("endswith"),
    CONTAINS("contains"),
    REGEX("re"),
    GREATER_THAN("greater_than"),
    LESS_THAN("less_than"),
    ALL("all");

    private final String value;


    ModifierType(String val) {
        value = val;

    }
    private static final Map<String, ModifierType> lookup = new HashMap<String, ModifierType>();

    //Populate the lookup table on loading time
    static
    {
        for(ModifierType t: ModifierType.values())
        {
            lookup.put(t.value, t);
        }
    }

    public static ModifierType getEnum(String val)
    {
        return lookup.get(val);
    }
}
