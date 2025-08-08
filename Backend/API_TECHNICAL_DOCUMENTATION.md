# MODAPTO Evaluation and Decision Support System - API Technical Documentation

## Table of Contents

1. [System Overview](#system-overview)
2. [Authentication & Security](#authentication--security)
3. [REST API Endpoints](#rest-api-endpoints)
    - [MODAPTO Modules Controller](#1-modapto-modules-controller)
    - [Order Controller](#2-order-controller)
    - [Optimization Controller](#3-optimization-controller)
    - [Simulation Controller](#4-simulation-controller)
    - [Predictive Maintenance Controller](#5-predictive-maintenance-controller)
4. [Data Transfer Objects (DTOs)](#data-transfer-objects-dtos)
5. [WebSocket Communication](#websocket-communication)
6. [Exception Handling](#exception-handling)

---

## System Overview

The MODAPTO Evaluation and Decision Support System (EDS) is a Spring Boot-based REST API that provides evaluation and decision support capabilities for manufacturing pilot cases. The system handles optimization, simulation, predictive maintenance, and module management functionalities.

**Base URL**: `/api/eds`
**WebSocket Endpoint**: `/eds/websocket`
**Authentication**: JWT-based OAuth2 Resource Server (Keycloak)

---

## Authentication & Security

All API endpoints require a valid JWT Bearer token, except for the Swagger documentation and WebSocket endpoints. The token must be included in the `Authorization` header as a Bearer token. You should retrieve a token either by Accessing MODAPTO Website and looking at the Local storage or implement one of the following requests:

```bash
curl --location 'https://services.modapto.atc.gr/api/users/authenticate' \
--header 'Content-Type: application/json' \
--data-raw '{
   "email":"[email]",
   "password": "[password]"
}'
```

---

## REST API Endpoints

### 1. MODAPTO Modules Controller

**Base Path**: `/api/eds/modules`

#### 1.1 Retrieve All Modules (Paginated)

```http
GET /api/eds/modules?page=0&size=10
```

**Success Response Example (200 OK)**:
```json
{
  "data": {
    "results": [
      {
        "id": "65a7b8e4c3d4e3f1a2b3c4d5",
        "moduleId": "CRF_ASSEMBLY_01",
        "name": "CRF Assembly Module",
        "endpoint": "http://crf-module-endpoint.com/api",
        "timestampDt": "2025-08-08T10:00:00",
        "timestampElastic": 1723111200000,
        "smartServices": [
          {
            "name": "CRF Optimization Service",
            "catalogueId": "CAT-CRF-OPT",
            "serviceId": "SVC-CRF-OPT-01",
            "endpoint": "http://crf-optimization-service.com/invoke"
          }
        ]
      }
    ],
    "totalPages": 1,
    "totalElements": 1,
    "lastPage": true
  },
  "errors": null,
  "message": "MODAPTO modules retrieved successfully",
  "success": true,
  "timestamp": "2025-08-08T12:00:00Z"
}
```

#### 1.2 Retrieve All Modules (Non-paginated)

```http
GET /api/eds/modules/all
```

Same response, not paginated

#### 1.3 Retrieve Module by ID

```http
GET /api/eds/modules/CRF_ASSEMBLY_01
```

Specific module, the response is only the Inner Object

#### 1.4 Retrieve Smart Services for Module

```http
GET /api/eds/modules/CRF_ASSEMBLY_01/smart-services
```

### 2. Order Controller

**Base Path**: `/api/eds/orders`

#### 2.1 Create Single Order

```http
POST /api/eds/orders/createOrder
```

**Request Body Example**:
```json
{
  "customer": "Global Tech Inc.",
  "documentNumber": "PO-2025-789",
  "orderof": [
    {
      "type": "MAIN_BOARD_ASSEMBLY",
      "quantity": 200,
      "pn": "MB-AS-V4",
      "expectedDeliveryDate": "2025-09-20"
    }
  ],
  "composedby": [
    {
      "type": "MICROCONTROLLER",
      "quantity": 200,
      "pn": "MCU-XR-32-BIT",
      "expectedDeliveryDate": "2025-09-01"
    },
    {
      "type": "POWER_SUPPLY_UNIT",
      "quantity": 200,
      "pn": "PSU-12V-5A",
      "expectedDeliveryDate": "2025-09-01"
    }
  ],
  "comments": "Expedite if possible."
}
```

**Success Response Example (201 Created)**:
```json
{
  "data": null,
  "errors": null,
  "message": "Order created successfully",
  "success": true,
  "timestamp": "2025-08-08T12:05:00Z"
}
```

#### 2.2 Create Multiple Orders

```http
POST /api/eds/orders/createOrders
```

**Request Body Example**: `[ OrderDto, ... ]`

#### 2.3 Retrieve Order by ID

```http
GET /api/eds/orders/65a7b8e4c3d4e3f1a2b3c4d6/pilot/CRF
```

**Success Response Example (200 OK)**:
```json
{
  "data": {
    "id": "65a7b8e4c3d4e3f1a2b3c4d6",
    "customer": "Global Tech Inc.",
    "documentNumber": "PO-2025-789",
    "orderof": [
      {
        "type": "MAIN_BOARD_ASSEMBLY",
        "quantity": 200,
        "pn": "MB-AS-V4",
        "expectedDeliveryDate": "2025-09-20"
      }
    ],
    "composedby": [
      {
        "type": "MICROCONTROLLER",
        "quantity": 200,
        "pn": "MCU-XR-32-BIT",
        "expectedDeliveryDate": "2025-09-01"
      }
    ],
    "comments": "Expedite if possible."
  },
  "errors": null,
  "message": "Order retrieved successfully",
  "success": true,
  "timestamp": "2025-08-08T12:10:00Z"
}
```

#### 2.4 Retrieve Paginated Orders

```http
GET /api/eds/orders/pilot/CRF?startDate=2025-08-01&endDate=2025-08-31
```

Same structure but with the pagination structure displayed previously and in the DTO Section

### 3. Optimization Controller

**Base Path**: `/api/eds/optimization`

All result files reference to the original files given by AUEB

#### 3.1 Retrieve Latest CRF Optimization Results

```http
GET /api/eds/optimization/pilots/crf/latest
```

**Success Response Example (200 OK)**:
```json
{
  "data": {
    "id": "crf_opt_res_123",
    "timestamp": "2025-08-08T11:30:00",
    "message": "Optimization successful",
    "productionModule": "CRF_ASSEMBLY_01",
    "optimization_results": {
      "exact": {
        "cost": 12345,
        "time_details": [
          {
            "component_picked": "RESISTOR-10K",
            "component_placed": "CAPACITOR-100NF",
            "distance": 50,
            "from": "Feeder1",
            "to": "PlacementHead1"
          }
        ]
      },
      "improvement_percentage": 15.5
    },
    "optimization_run": true,
    "solutionTime": 1200,
    "totalTime": 1500
  },
  "message": "Latest CRF Optimization results retrieved successfully",
  "success": true,
  "timestamp": "2025-08-08T12:15:00Z"
}
```

#### 3.2 Upload SEW Production Schedule

```http
POST /api/eds/optimization/pilots/sew/uploadProductionSchedule
```

**Request Body Example (`SewProductionScheduleDto`)**:
```json
{
  "data": {
    "2025-08-11": {
      "newlayout": { ... },
      "workers": { ... },
      "general": { ... },
      "orders": { ... },
      "processTimes": { ... },
      "givenOrder": [ "order1", "order2" ]
    }
  }
}
```

Structure of Input (Where JsonProperty is set, then this variable name is used in the Input):

```java
package gr.atc.modapto.dto.serviceInvocations;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SewProductionScheduleDto {

    private Map<String, DailyDataDto> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyDataDto {

        @JsonProperty("newlayout")
        private NewLayoutDto newLayout;

        private WorkersDto workers;

        private GeneralDto general;

        private Map<String, WorkingOrderDto> orders;

        private Map<String, Map<String, Map<String, Integer>>> processTimes;

        private List<String> givenOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewLayoutDto {

        private Map<String, StageDto> stages;

        @JsonProperty("transTimes")
        private Map<String, Map<String, Integer>> transitionTimes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageDto {

        @JsonProperty("Cells")
        private Map<String, ProductionCellDto> cells;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionCellDto {

        @JsonProperty("WIP_in")
        private int wipIn;

        @JsonProperty("WIP_out")
        private int wipOut;

        @JsonProperty("sug_w")
        private int suggestedWorkers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkersDto {

        private Map<String, Double> productivity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralDto {

        private int numOrders;
        private int numJobs;
        private int numStages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkingOrderDto {

        private String name;

        private Map<String, JobConnectDto> jobs = new HashMap<>();
        
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobConnectDto {

        private List<String> jobConnect;
    }

}

```

Example of this file can be located here: <https://atcgr.sharepoint.com/sites/MODAPTO/_layouts/15/download.aspx?UniqueId=b02bd78243644b9e84b3190889b469c3&e=t9KkVh

### 4. Simulation Controller

**Base Path**: `/api/eds/simulation`

Data formats are according to AUEB input

#### 4.1 Retrieve Latest SEW Simulation Results

```http
GET /api/eds/simulation/pilots/sew/latest
```

**Success Response Example (200 OK)**:
```json
{
  "data": {
    "id": "sew_sim_res_456",
    "timestamp": "2025-08-08T11:45:00",
    "productionModule": "SEW_MANUFACTURING_01",
    "simulationData": {
      "makespan": {
        "Current": 8.5,
        "Simulated": 7.2,
        "Difference": -1.3,
        "Difference_percent": -15.3
      },
      "machine_utilization": {
        "Current": 75.0,
        "Simulated": 85.0,
        "Difference": 10.0,
        "Difference_percent": 13.3
      },
      "throughput_stdev": {
        "Current": 1.2,
        "Simulated": 0.8,
        "Difference": -0.4,
        "Difference_percent": -33.3
      }
    }
  },
  "message": "Latest SEW Simulation results retrieved successfully",
  "success": true,
  "timestamp": "2025-08-08T12:20:00Z"
}
```

### 5. Predictive Maintenance Controller

**Base Path**: `/api/eds/maintenance`

#### 5.1 Upload CORIM File

```http
POST /api/eds/maintenance/uploadCorimFile
```

Sample input Excel file can be located here: <https://atcgr.sharepoint.com/:x:/r/sites/MODAPTO/Shared%20Documents/General/WPs/WP3%20Modular%20Manufacturing%20Use%20Cases%20%26%20Lessons%20Learnt%20(CRF)/UC2%20-%20SEW/Smart%20Services%20Inputs%20-%20Outputs%20(%20+%20Data%20Samples)/SEW%20Data/2025-07-22_CORIM%202024-2025%20MOTP-G1.xlsx?d=w8ce29e38307f4b9581019608af565273&csf=1&web=1&e=LkJGrC>

- **Request**: `multipart/form-data` with `file` parameter.

#### 5.2 Upload SEW Components List

```http
POST /api/eds/maintenance/uploadComponentsList
```

**Request Body Example**:
```json
[
  {
    "Stage": "Assembly",
    "Cell": "A-01",
    "Module": "Motor Assembly",
    "Module ID": "MOD-MOT-01",
    "Alpha": 1.5,
    "Beta": 2000.0,
    "Average maintenance duration": 60.0,
    "MTBF": 5000.0,
    "Last Maintenance Action Time": null // This is calculated automatically should no provided
  }
]
```

#### 5.3 Invoke Grouping-Based Predictive Maintenance

```http
POST /api/eds/maintenance/predict/grouping-maintenance
```

**Request Body Example**:
```json
{
  "moduleId": "MOD-MOT-01",
  "smartServiceId": "SVC-PDM-GROUP-01",
  "setupCost": 500.0,
  "downtimeCostRate": 200.0,
  "noRepairmen": 2,
  "components": null // It is provided automatically
  ]
}
```

#### 5.4 Invoke Threshold-Based Predictive Maintenance

```http
POST /api/eds/maintenance/predict/threshold-based-maintenance
```

**Request Body Example**:
```json
{
  "moduleId": "MOD-MOT-01",
  "smartServiceId": "SVC-PDM-THRESH-01",
  "events": null, // It is provided automatically by the business logic
  "parameters": {
    "module_ID": "MOD-MOT-01",
    "components_ID": ["COMP-BRG-0123"],
    "window_size": 30,
    "inspection_threshold": 5,
    "replacement_threshold": 10
  }
}
```

#### 5.5 Create Process Drift

```http
POST /api/eds/maintenance/process-drifts/create
```

**Request Body Example**:
```json
{
  "Stage": "Final Assembly",
  "Cell": "F-05",
  "Module description": "Robot Arm Calibration",
  "Module ID": "MOD-ROBOT-02",
  "Component": "Positioning Sensor",
  "Component ID": "SENS-POS-XYZ",
  "Failure description": "Drift in Y-axis calibration",
  "Maintenance Action performed": "Recalibration sequence initiated.",
  "Name": "system-auto-detect",
  "TS request creation": "2025-08-08T14:00:00",
  "TS Intervention started": "2025-08-08T14:01:00"
}
```

---

## Data Transfer Objects (DTOs)

This section describes the main data structures used in the API. Field names reflect the JSON properties.

- **`OrderDto`**: Uses `orderof` for assemblies and `composedby` for components.
- **`MaintenanceDataDto`**: Uses `PascalCase` for many fields (e.g., `Stage`, `Cell`, `Module ID`).
- **Result DTOs**: Often use `snake_case` for nested properties (e.g., `optimization_results`, `time_details`).

Please refer to the specific DTO definitions in the source code for detailed field mappings.

---

## WebSocket Communication

**Endpoint**: `/eds/websocket`

Clients can subscribe to topics to receive real-time updates for asynchronous operations like predictive maintenance.

**Example Subscription (JavaScript with STOMP)**:
```javascript
stompClient.subscribe('/topic/events/sew-grouping-predictive-maintenance', (message) => {
  const result = JSON.parse(message.body);
  console.log('Received Grouping PdM Result:', result);
});
```

The **topics** which are used are the following under the ***/topic/events/*** namespace:
- crf-simulation-results
- crf-optimization-results
- sew-simulation-results
- sew-optimization-results
- sew-threshold-based-predictive-maintenance
- sew-grouping-predictive-maintenance
- sew-self-awareness
- crf-self-awareness

---

## Exception Handling

The API provides standardized error responses.

**Generic Error Response (4xx/5xx)**:
```json
{
  "data": null,
  "errors": {
    "fieldName": "Description of the error for this field."
  },
  "message": "A high-level summary of the error.",
  "success": false,
  "timestamp": "2025-08-08T14:05:00Z"
}
```

**Example Validation Error (400 Bad Request)**:
```json
{
  "data": null,
  "errors": {
    "customer": "must not be empty",
    "orderof": "must not be empty"
  },
  "message": "Validation failed",
  "success": false,
  "timestamp": "2025-08-08T14:10:00Z"
}
```