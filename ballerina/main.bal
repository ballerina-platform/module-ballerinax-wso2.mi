import ballerina/io;

public function main() {
    io:println("Hello, World!");
}

public type ConnectorInformation record {|
    string iconPath = "";
|};

public const annotation ConnectorInformation ConnectorInfo on function;
