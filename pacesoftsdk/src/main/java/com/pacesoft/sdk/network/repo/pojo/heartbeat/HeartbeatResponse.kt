package com.pacesoft.sdk.network.repo.pojo.heartbeat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HeartbeatResponse(
    @SerializedName("CustomerList") val CustomerList: List<Customer>,
    @SerializedName("PayDetails") val PayDetails: PayDetails,
    @SerializedName("PayResponse") val PayResponse: PayResponse,
    @SerializedName("ResponseCode") val ResponseCode: String,
    @SerializedName("ResponseText") val ResponseText: String,
    @SerializedName("TransactionStatusResponse") val TransactionStatusResponse: TransactionStatusResponse
): Parcelable {

    @Parcelize
    data class BeaconDetails(
        @SerializedName("MACID") val MACID: String,
        @SerializedName("Name") val Name: String,
        @SerializedName("Range") val Range: String
    ) : Parcelable
}

@Parcelize
data class Customer(
    @SerializedName("BeaconDetails") val BeaconDetails: BeaconDetails,
    @SerializedName("MobileNumber") val MobileNumber: String
) : Parcelable

@Parcelize
data class PayDetails(
    @SerializedName("Amount")  val Amount: String,
    @SerializedName("CustomerId") val CustomerId: String
) : Parcelable

@Parcelize
data class PayResponse(
    @SerializedName("SaleResponseJSON")  val SaleResponseJSON: String
) : Parcelable

@Parcelize
data class TransactionStatusResponse(
    @SerializedName("CustomerId") val CustomerId: String,
    @SerializedName("Status") val Status: String
) : Parcelable


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

*/
