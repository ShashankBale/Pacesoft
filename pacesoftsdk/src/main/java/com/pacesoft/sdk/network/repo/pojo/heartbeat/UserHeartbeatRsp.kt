package com.pacesoft.sdk.network.repo.pojo.heartbeat


import com.google.gson.annotations.SerializedName

data class UserHeartbeatRsp(
    @SerializedName("ResponseCode") val responseCode: String? = null,
    @SerializedName("ResponseText") val responseText: String? = null,
    @SerializedName("PayDetails") val payDetails: PayDetails? = null,
    @SerializedName("PayResponse") val payResponse: PayResponse? = null,
    @SerializedName("CustomerList") val customerList: List<Customer?>? = null,
    @SerializedName("TransactionStatusResponse") val transactionStatusResponse: TransactionStatusResponse? = null
) {
    data class PayDetails(
        @SerializedName("Amount") val amount: String? = null,
        @SerializedName("CustomerId") val customerId: String? = null
    )

    data class PayResponse(
        @SerializedName("SaleResponseJSON") val saleResponseJSON: String? = null
    )

    data class Customer(
        @SerializedName("MobileNumber") val mobileNumber: String? = null,
        @SerializedName("BeaconDetails") val beaconDetails: BeaconDetails? = null
    ) {
        data class BeaconDetails(
            @SerializedName("Range") val range: String? = null,
            @SerializedName("MACID") val macId: String? = null,
            @SerializedName("Name") val name: String? = null
        )
    }

    data class TransactionStatusResponse(
        @SerializedName("CustomerId") val customerId: String? = null,
        @SerializedName("Status") val status: String? = null
    )
}

/*
{
    "ResponseCode": "00",
    "ResponseText": "Approval",
    "PayDetails": {
        "Amount": "4.99",
        "CustomerId": "918369274899"
    },

	    "PayResponse": {
        "SaleResponseJSON": "{\"approved\":true,\"responseCode\":\"00\",\"responseDescription\":\"APPROVAL AXS148\",\"authCode\":\"AXS148\",\"transactionId\":\"000000321000149\",\"referenceId\":\"222409501207\",\"transactionType\":null,\"transactionHash\":null,\"timestamp\":\"2022-08-12T09:58:22.6694869+00:00\",\"entryMethod\":\"swiped\",\"paymentType\":null,\"maskedPan\":\"****-****-****-1003\",\"cardHolder\":\"\",\"partialAuth\":false,\"currencyCode\":\"USD\",\"requestedAmount\":4.99,\"authorizedAmount\":4.99,\"surchargeAmount\":0,\"PAR\":null,\"version\":null,\"token\":null,\"receiptSuggestions\":{\"AID\":null,\"ARQC\":null,\"IAD\":null,\"TVR\":null,\"TSI\":null,\"merchantName\":\"DeviceServices\",\"applicationLabel\":null,\"requestSignature\":false,\"maskedPan\":\"****-****-****-1003\",\"authorizedAmount\":null,\"transactionType\":null,\"entryMethod\":\"swiped\"},\"terminalTransactionId\":10341731475390528,\"emvData\":null,\"avsResultCode\":\"0\",\"cvvResultCode\":null,\"avsResponse\":\"Approved\"}"
    },
	"CustomerList": [
        {
            "MobileNumber": "918369274899",
            "BeaconDetails": {
                "Range": "-12",
                "MACID": "123456-123456",
                "Name": "Beacon 3101"
            }
        },
        {
            "MobileNumber": "918369274899",
            "BeaconDetails": {
                "Range": "-31",
                "MACID": "789456-789456",
                "Name": "Beacon 0131"
            }
        }
    ],
	    "TransactionStatusResponse": {
        "CustomerId": "918369274899",
        "Status": "IN PROCESS"
    }

}
* */