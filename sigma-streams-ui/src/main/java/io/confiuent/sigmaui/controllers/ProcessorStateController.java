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

package io.confiuent.sigmaui.controllers;

import io.confiuent.sigmaui.config.SigmaUIProperties;
import io.confluent.sigmarules.appState.SigmaAppInstanceState;
import io.confluent.sigmarules.appState.SigmaAppInstanceStore;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessorStateController {
    @Autowired
    SigmaUIProperties properties;

    private SigmaAppInstanceStore stateStore;

    @PostConstruct
    private void initialize() {
        stateStore = new SigmaAppInstanceStore(properties.getProperties());
    }

    @GetMapping({"/instanceIDs"})
    public Set<String> getInstanceIDs() {
        return stateStore.getInstanceIDs();
    }

    @GetMapping({"/processorStates"})
    public List<SigmaAppInstanceState> getProcessorStates() {
        return stateStore.getStates();
    }

}
