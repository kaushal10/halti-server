/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications for Halti usage made by Emblica Ltd.
 *
 */
package emblica.halti.domain;


public class MachineCapacity {

    private Machine machine;
    private Resource resource;

    private long maximumCapacity;
    private long safetyCapacity;

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public long getMaximumCapacity() {
        return maximumCapacity;
    }

    public void setMaximumCapacity(long maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public long getSafetyCapacity() {
        return safetyCapacity;
    }

    public void setSafetyCapacity(long safetyCapacity) {
        this.safetyCapacity = safetyCapacity;
    }

    public boolean isTransientlyConsumed() {
        return resource.isTransientlyConsumed();
    }

}
