package com.pacesoft.sdk.network.repo.pojo.heartbeat


import com.google.gson.annotations.SerializedName

data class UserHeartbeatReq(
    @SerializedName("userType") val userType: String? = null,
    @SerializedName("Action") val action: String? = null,
    @SerializedName("RequestId") val requestId: String? = null,
    @SerializedName("UserTypeId") val userTypeId: String? = null,
    @SerializedName("GeoFencingDetails") val geoFencingDetails: List<GeoFencingDetail?>? = null,
    @SerializedName("BeaconInRange") val beaconInRange: List<BeaconInRange?>? = null,
    @SerializedName("RequestPayToCustomer") val requestPayToCustomer: RequestPayToCustomer? = null,
    @SerializedName("TransactionStatus") val transactionStatus: TransactionStatus? = null,
    @SerializedName("Pay") val pay: Pay? = null,
    @SerializedName("DeclinePay") val declinePay: DeclinePay? = null
) {
    data class GeoFencingDetail(
        @SerializedName("BeaconDetails") val beaconDetails: BeaconDetails
    ) {
        data class BeaconDetails(
            @SerializedName("Range") val range: String,
            @SerializedName("MACID") val macId: String,
            @SerializedName("Name") val name: String
        )
    }

    data class BeaconInRange(
        @SerializedName("BeaconDetails") val beaconDetails: BeaconDetails? = null
    ) {
        data class BeaconDetails(
            @SerializedName("Range") val range: String,
            @SerializedName("MACID") val macId: String,
            @SerializedName("Name") val name: String
        )
    }

    data class RequestPayToCustomer(
        @SerializedName("Amount") val amount: String,
        @SerializedName("CustomerId") val customerId: String
    )

    data class TransactionStatus(
        @SerializedName("CustomerId") val customerId: String
    )

    data class Pay(
        @SerializedName("CustomerId") val customerId: String,
        @SerializedName("SaleRequestJSON") val saleRequestJSON: String
    )

    data class DeclinePay(
        @SerializedName("CustomerId") val customerId: String,
        @SerializedName("ReasonForDecline") val reasonForDecline: String
    )
}

/*
{
    "userType": "Customer",
    "Action": "GeoFencingDetails",
    "RequestId": "{{RequestId}}",
    "UserTypeId": "918369274899",
    "GeoFencingDetails": [
        {
            "BeaconDetails": {
                "Range": "-12",
                "MACID": "123456-123456",
                "Name": "Beacon 3101"
            }
        },
        {
            "BeaconDetails": {
                "Range": "-31",
                "MACID": "789456-789456",
                "Name": "Beacon 0131"
            }
        }
    ],
   "BeaconInRange": [
        {
            "BeaconDetails": {
                "Range": "-01",
                "MACID": "123456-123456",
                "Name": "Beacon 3101"
            }
        },
        {
            "BeaconDetails": {
                "Range": "-91",
                "MACID": "789456-789456",
                "Name": "Beacon 0131"
            }
        }
    ],
     "RequestPayToCustomer": {
        "Amount": "4.99",
        "CustomerId": "918369274899"
    },
    "TransactionStatus": {
        "CustomerId": "918369274899"
    },
       "Pay": {
        "CustomerId": "918369274899",
        "SaleRequestJSON": "{\"Type\":4,\"account\":{\"billTo\":{\"address\":{}},\"expirationDate\":\"1224\",\"pan\":\"374245002771003\",\"trackData\":\"374245002771003=241270215021234500000\"},\"amount\":4.99,\"currencyCode\":\"USD\",\"shipTo\":{\"address\":{}},\"tipAmount\":0}"
    },
    "DeclinePay": {
        "CustomerId": "918369274899",
        "ReasonForDecline": "Wrong Requested"
    }
}
* */