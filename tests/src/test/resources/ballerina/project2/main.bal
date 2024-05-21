public function test(xml xmlA, xml xmlB, xml xmlC) returns xml {
    xml ans = xml `<may21><time>9:31</time>${xmlA}${xmlB}${xmlC}</may21>`;
    return ans;
}
