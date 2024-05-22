public function test(xml xmlA, xml xmlB, xml xmlC, xml xmlD) returns xml {
    return xml `<may22>${xmlA}${xmlB}${xmlC}${xmlD}</may22>`;
}
