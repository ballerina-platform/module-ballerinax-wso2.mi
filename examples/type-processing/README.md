## Overview

This sample demonstrates the supported types of the Ballerina MI connector SDK. A Ballerina function with parameters and return type of `boolean`, `string`, `int`, `float`, `decimal`, `xml`, and `json` is supported by the MI SDK.
This scenario illustrates a content-based routing use case where a JSON payload containing a `type` property is sent to an API. Based on the value of this `type` property, a corresponding Ballerina function is invoked.

A json payload of following format is sent to the endpoint

```json
{
    "type": "integer",
    "val": 21
}
```

For the type field it is possible to send `boolean`, `string`, `int`, `float`, `decimal`, `xml`, and `json`.

This would output a result in the following format.

```json
{
   "result": 42
}
```

## Steps to Invoke the Sample

Follow these steps to invoke the sample using the connector:

1. The `bal-type-processing` folder contains the Ballerina code for the connector. Invoke the following command to generate the connector:

    ```bash
    bal mi -i <ballerina-project>
    ```

2. A ZIP file of the connector will be generated. Add this ZIP file to the MI project inside the folder `mi-type-processing` following the approach described [here](https://mi.docs.wso2.com/en/latest/develop/creating-artifacts/adding-connectors/).

3. Once the connector is added, run the MI project.

4. Send an HTTP POST request to the following resource with a payload as specified:

   ```bash
   curl --location 'http://localhost:8290/type-processing' \
   --header 'Content-Type: application/json' \
   --data '{
      "type": "decimal",
      "val": 5.3
   }'
   ```

   Output:

   ```json
   {
      "result":15.3
   }
   ```
