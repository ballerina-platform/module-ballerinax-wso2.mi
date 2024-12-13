import wso2/mi;

@mi:ConnectorInfo
public function calculateTotal(xml invoice) returns xml {
    xml<xml:Element> prices = invoice/**/<price>;
    int total = from xml:Element element in prices
        let int|error price = int:fromString(element.data())
        where price is int
        collect sum(price);
    return xml `<total>${total}</total>`;
}
