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

import ballerinax/mi;

public type Rec record {|
    string a;
    int b;
|};

@mi:ConnectorInfo
public function f3(int a) {

}

function f4() {

}

public function f5(Rec rec) {

}

public function f6(int a) returns Rec {
    return {a: "a", b: 1};
}

public function f7(int|string a) {

}

public function f9(error e) {

}

public function f10() returns error? {

}

public function f11(int? a) {

}
