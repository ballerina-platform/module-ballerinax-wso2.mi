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

listener http:Listener httpListener = check new (9090);
http:Listener httpListener2 = check new (9091);

http:Service helloService = service object {
    resource function get greeting() returns string {
        return "dynamic listener!";
    }
};

var obj1 = object {
    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function attach() returns error? {
    }

    public function detach() returns error? {
    }
};


var obj2 = object {
    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

};

var obj3 = object {
    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function attach() returns error? {
    }

    public function detach() returns error? {
    }

    public function bar() {

    }
};

class ListenerClass {
    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function attach() returns error? {
    }

    public function detach() returns error? {
    }
}

@mi:ConnectorInfo {
}
function registerServiceDynamically() {
    http:Listener|error? httpListener3 = new (9092);
    error? e = httpListener2.attach(helloService, "foo/bar");
    e = httpListener2.'start();
    runtime:registerListener(httpListener2);
    ListenerClass cls = new();
}
