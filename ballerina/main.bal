import ballerina/io;

public function main() {
    io:println("Hello, World!");
}

public type ConnectorInformation record {|
    string iconPath = "";
    string name = "";
|};

public const annotation ConnectorInformation ConnectorInfo on function;
