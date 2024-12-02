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

import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import static io.ballerina.tools.diagnostics.DiagnosticSeverity.ERROR;
import static io.ballerina.tools.diagnostics.DiagnosticSeverity.INTERNAL;

public enum DiagnosticCode {
    UNSUPPORTED_PARAM_TYPE("MIE001", "unsupported parameter type found", ERROR),
    UNSUPPORTED_RETURN_TYPE("MIE002", "unsupported return type found", ERROR),
    SERVICE_DEF_NOT_ALLOWED("MIE003",
            "service definition is not allowed when `ballerinax/mi` connector is in use", ERROR),
    LISTENER_DECLARATION_NOT_ALLOWED("MIE004",
            "listener declaration is not allowed when `ballerinax/mi` connector is in use", ERROR),
    LISTENER_SHAPE_VAR_NOT_ALLOWED("MIE005",
            "defining variables with a type that has the shape of `Listener` is not allowed when the `ballerinax/mi` " +
                    "connector is in use.", ERROR),
    MI_ANNOTATION_ADD("MI_HINT_001", "MI Annotation can be added", INTERNAL)
    ;

    private final String diagnosticId;
    private final String message;
    private final DiagnosticSeverity severity;

    DiagnosticCode(String diagnosticId, String message, DiagnosticSeverity severity) {
        this.diagnosticId = diagnosticId;
        this.message = message;
        this.severity = severity;
    }

    public String diagnosticId() {
        return diagnosticId;
    }
    public String message() {
        return message;
    }
    public DiagnosticSeverity severity() {
        return severity;
    }
}
