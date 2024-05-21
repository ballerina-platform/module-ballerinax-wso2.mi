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

package io.ballerina.stdlib.mi;

public class ModuleInfo {
    private static String ORG_NAME = "BALLERINA_ORG_NAME";
    private static String MODULE_NAME = "BALLERINA_MODULE_NAME";
    private static String MODULE_VERSION = "BALLERINA_MODULE_VERSION";

    public String getOrgName() {
        return ORG_NAME;
    }

    public String getModuleName() {
        return MODULE_NAME;
    }

    public String getModuleVersion() {
        return MODULE_VERSION;
    }

    public ModuleInfo() {}

    public ModuleInfo(String orgName, String moduleName, String moduleVersion) {
        ORG_NAME = orgName;
        MODULE_NAME = moduleName;
        MODULE_VERSION = moduleVersion;
    }
}
