// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/graphql;
import ballerina/http;
import ballerina/lang.runtime;
import ballerinax/mi;

service / on new http:Listener(9090) {
    resource function get greeting() returns string {
        return "ballerina mi connector http service!";
    }
}

service /graphql on new graphql:Listener(9091) {
    resource function get greeting() returns string {
        return "ballerina mi connector graphql service";
    }
}

service class EchoService {
    remote function greeting() returns string {
        return "hello";
    }
}

http:Listener httpListener = check new (9090);

http:Service helloService = service object {
    resource function get greeting() returns string {
        return "dynamic listener!";
    }
};

@mi:ConnectorInfo {
}
function registerServiceDynamically() {
    error? e = httpListener.attach(helloService, "foo/bar");
    e = httpListener.'start();
    runtime:registerListener(httpListener);
}
