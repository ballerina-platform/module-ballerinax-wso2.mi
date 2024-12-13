/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.mi.plugin.model;

import io.ballerina.stdlib.mi.plugin.Utils;

import java.io.File;
import java.util.ArrayList;

public class Component extends ModelElement {

    public static final String ANNOTATION_QUALIFIER = "Operation";
    private static final String TYPE_NAME = "component";
    private String name;
    private String description = "just a description";
    private ArrayList<Param> params = new ArrayList<>();
    private ArrayList<FunctionParam> balFuncParams = new ArrayList<>();
    private String balFuncReturnType;
    private Connector parent;

    public Component(String name) {
        this.name = name;
    }

    public Component(String name, ArrayList<Param> params) {
        this.name = name;
        this.params = params;
    }

    public ArrayList<Param> getParams() {
        return params;
    }

    public void setParam(Param param) {
        this.params.add(param);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Connector getParent() {
        return parent;
    }

    public void setParent(Connector parent) {
        this.parent = parent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return "component";
    }

    public String getBalFuncReturnType() {
        return balFuncReturnType;
    }

    public void setBalFuncReturnType(String returnType) {
        this.balFuncReturnType = returnType;
    }

    public ArrayList<FunctionParam> getBalFuncParams() {
        return balFuncParams;
    }

    public void addBalFuncParams(FunctionParam param) {
        this.balFuncParams.add(param);
    }

    public void generateInstanceXml(File connectorFolder) {
        File file = new File(connectorFolder, this.getName());
        if (!file.exists()) {
            file.mkdir();
        }
        Utils.generateXml(TYPE_NAME, file + File.separator + "component", this);
    }

    public void generateTemplateXml(File connectorFolder) {
        File file = new File(connectorFolder, this.getName());
        if (!file.exists()) {
            file.mkdir();
        }
        Utils.generateXml(TYPE_NAME + "_template", file + File.separator + this.getName() + "_template", this);
    }

    public void generateUIJson(File connectorFolder) {
        File file = new File(connectorFolder, "uischema");
        if (!file.exists()) {
            file.mkdir();
        }
        Utils.generateJson(TYPE_NAME, file + File.separator + this.name, this);
    }
}
