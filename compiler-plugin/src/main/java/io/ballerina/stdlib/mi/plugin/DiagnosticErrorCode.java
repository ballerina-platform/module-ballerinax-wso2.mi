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

package io.ballerina.stdlib.mi.plugin;

public enum DiagnosticErrorCode {
    UNSUPPORTED_PARAM_TYPE("MIE001", "unsupported parameter type found"),
    UNSUPPORTED_RETURN_TYPE("MIE002", "unsupported return type found"),
    SERVICE_DEF_NOT_ALLOWED("MIE003",
            "service definition is not allowed when `ballerinax/mi` connector is in use"),
    DYNAMIC_SERVICE_REGISTER_NOT_ALLOWED("MIE004",
            "dynamic listener registering not allowed when `ballerinax/mi` connector is in use");
    ;

    private final String diagnosticId;
    private final String message;
    DiagnosticErrorCode(String diagnosticId, String message) {
        this.diagnosticId = diagnosticId;
        this.message = message;
    }

    public String diagnosticId() {
        return diagnosticId;
    }
    public String message() {
        return message;
    }
}
