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

import java.util.ArrayList;
import java.util.Vector;

public class InfiniteLoop {

    /*
     * This will cause the program to hang...
     *
     * Taken from:
     * http://www.exploringbinary.com/java-hangs-when-converting-2-2250738585072012e-308/
     */
    public static void main(String[] args) {
        double d = Double.parseDouble("2.2250738585072012e-308");

        // unreachable code
        System.out.println("Value: " + d);
    }
}
