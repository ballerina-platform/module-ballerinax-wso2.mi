# Ballerina Connector for WSO2 Micro Integrator

## Overview

The Ballerina connector for WSO2 Micro Integrator allows you to run Ballerina transformations on WSO2 Micro Integrator. 
This integration enables you to leverage the powerful transformation capabilities of Ballerina within 
the environment of WSO2 Micro Integrator.

## Steps to Create Ballerina Connector

### Pull `mi` Tool

First, you need to pull the `mi` tool which is used to create the Ballerina connector.

```bash
bal pull mi
```

### Write Ballerina Transformation

Next, you need to write the Ballerina transformation in a Ballerina project. 
Create a new Ballerina project or use an existing one and write your transformation logic.
For example,

```
@mi:ConnectorInfo {}
public function GPA(xml rawMarks, xml credits) returns xml {
   // Your logic to calculate the GPA
}
```

Ballerina function that contains `@mi:ConnectorInfo` annotation maps with a component in Ballerina connector.

### Generate the connector

Finally, use the `bal mi` command to generate the Ballerina connector for the WSO2 Micro Integrator.

```bash
bal mi <path_to_ballerina_project>
```

Above command generates the connector zip in the same location.

## Local Build

1. Clone the repository [module-ballerinax-wso2.mi](https://github.com/ballerina-platform/module-ballerinax-wso2.mi.git)

2. Build the compiler plugin and publish locally:

   ```bash
   ./gradlew clean :mi-ballerina:localPublish
   ```

3. Build the tool and publish locally:

   ```bash
   ./gradlew clean :tool-mi:localPublish 
   ```

### Run tests

   ```bash
   ./gradlew test
   ```

## Contribute to Ballerina

As an open-source project, Ballerina welcomes contributions from the community.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of conduct

All the contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).
