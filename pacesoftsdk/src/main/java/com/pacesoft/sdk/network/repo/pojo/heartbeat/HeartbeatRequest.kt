package com.pacesoft.sdk.network.repo.pojo.heartbeat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HeartbeatRequest(
    @SerializedName("Action") val Action: String,
    @SerializedName("BeaconInRange") val BeaconInRange: List<BeaconDetails>?,
    @SerializedName("DeclinePay") val DeclinePay: DeclinePay?,
    @SerializedName("GeoFencingDetails") val GeoFencingDetails: List<BeaconDetails>?,
    @SerializedName("Pay") val Pay: Pay?,
    @SerializedName("RequestId") val RequestId: String,
    @SerializedName("RequestPayToCustomer") val RequestPayToCustomer: RequestPayToCustomer?,
    @SerializedName("TransactionStatus") val TransactionStatus: TransactionStatus?,
    @SerializedName("UserTypeId") val UserTypeId: String,
    @SerializedName("userType") val userType: String
):Parcelable

@Parcelize
data class BeaconInRange(
    @SerializedName("BeaconDetails")  val  BeaconDetails: List<BeaconDetails>
):Parcelable

@Parcelize
data class DeclinePay(
    @SerializedName("CustomerId") val CustomerId: String,
    @SerializedName("ReasonForDecline") val ReasonForDecline: String
):Parcelable

@Parcelize
data class GeoFencingDetail(
    @SerializedName("BeaconDetails") val BeaconDetails: List<BeaconDetails>
):Parcelable

@Parcelize
data class Pay(
    @SerializedName("CustomerId") val CustomerId: String,
    @SerializedName("SaleRequestJSON") val SaleRequestJSON: String
):Parcelable

@Parcelize
data class RequestPayToCustomer(
    @SerializedName("Amount") val Amount: String,
    @SerializedName("CustomerId") val CustomerId: String
):Parcelable

@Parcelize
data class TransactionStatus(
    @SerializedName("CustomerId") val CustomerId: String
):Parcelable

@Parcelize
data class BeaconDetails(
    @SerializedName("MACID")  val MACID: String,
    @SerializedName("Name")  val Name: String,
    @SerializedName("Range")  val Range: String
):Parcelable
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

*/
