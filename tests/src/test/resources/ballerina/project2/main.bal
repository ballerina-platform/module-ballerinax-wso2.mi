public function test(xml xmlA, xml xmlB, xml xmlC) returns xml {
    xml ans = xml `<may21><time>9:31</time>${xmlA}${xmlB}${xmlC}</may21>`;
    return ans;
}

public function testInt(int i1, int i2) returns int {
    return i1 + i2;
}

public function testString(string s1, string s2) returns string {
    return concatStr(s1, s2);
}

function concatStr(string s1, string s2) returns string {
    return string:concat(s1, s2);
}

public function testBoolean(boolean b1, boolean b2) returns boolean {
    return b1 && b2;
}

function testEmpty() {
}

public function testFloat(float f1, float f2) returns float {
    return f1 + f2;
}

function testJson(json j1, json j2) returns json {
    json|error mergedJson = j1.mergeJson(j2);
    if (mergedJson is error) {
        return {"status": "error"};
    } else {
        return mergedJson;
    }
}
