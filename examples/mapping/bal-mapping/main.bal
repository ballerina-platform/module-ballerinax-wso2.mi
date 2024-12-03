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

type Patient record {
    string patientType;
    string patientId;
    string version;
    string lastUpdatedOn;
    string originSource;
    Description description;
    Identifier[] identifiers;
    string firstName;
    string lastName;
    string gender;
    LocationDetail[] locationDetail;
};

type Identifier record {
    IdType id_type;
    string id_value;
};

type IdType record {
    Code[] codes;
};

type Code record {
    string system_source;
    string identifier_code;
};

type Description record {
    string status;
    string details?;
};

type LocationDetail record {
    string nation?;
    string town?;
    string region?;
    string zipCode?;
    string identifier?;
    string province?;
};

type ResponseResource record {
    string resourceId;
    string version;
};

@mi:ConnectorInfo
public function mapPatient(json payload) returns json {
    Patient|error patient = payload.cloneWithType();
    if patient is error {
        return "";
    }
    return {
        name: [
            {
                given: [patient.firstName],
                family: patient.lastName
            }
        ],
        meta: {
            versionId: patient.'version,
            lastUpdated: patient.lastUpdatedOn,
            'source: patient.originSource
        },
        text: {
            div: patient.description.details ?: "",
            status: patient.description.status

        },
        gender: patient.gender,
        identifier: [
            {
                system: patient.identifiers[0].id_type.codes[0].system_source,
                value: patient.identifiers[0].id_value
            }
        ],
        address: from var {nation, town, region, province, zipCode, identifier} in patient.locationDetail
            select {
                country: nation,
                city: town,
                district: region,
                state: province,
                postalCode: zipCode,
                id: identifier
            },
        id: patient.patientId
    };
}
