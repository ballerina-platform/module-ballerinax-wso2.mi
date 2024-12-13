import wso2/mi;

@mi:Operation {
}
public function test(xml xmlA, xml xmlB, xml xmlC) returns xml {
    xml ans =xml `<apr30>8:585555551</apr30>`;
    return xmlA + ans;
}
