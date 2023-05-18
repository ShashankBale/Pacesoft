package com.pacesoft.sdk.network.repo.pojo.trasaction.sale

import com.google.gson.annotations.SerializedName

data class SaleTxnReq(
    @SerializedName("currencyCode") var currencyCode: String? = "",
    @SerializedName("amount") var amount: Double? = 0.0,
    @SerializedName("tipAmount") var tipAmount: Int? = 0,
    @SerializedName("shipTo") var shipTo: ShipTo? = ShipTo(),
    @SerializedName("account") var account: Account? = Account(),
    @SerializedName("descriptor") var descriptor: String? = "",
    @SerializedName("Type") var type: Int? = 4,
    @SerializedName("accounttoken") var accounttoken: String? = ""
) {
    data class ShipTo(
        @SerializedName("firstName") var firstName: String? = "",
        @SerializedName("lastName") var lastName: String? = "",
        @SerializedName("address") var address: Address? = Address(),
        @SerializedName("phoneNumber") var phoneNumber: String? = "",
        @SerializedName("emailAddress") var emailAddress: String? = ""
    )

    data class Account(
        @SerializedName("billTo") var billTo: BillTo? = BillTo(),
        @SerializedName("pan") var pan: String? = "",
        @SerializedName("expirationDate") var expirationDate: String? = "",
        @SerializedName("cvv") var cvv: String? = "",
        @SerializedName("trackData") var trackData: String? = "",
        @SerializedName("pinBlock") var pinBlock: String? = "",
        @SerializedName("PAR") var par: String? = "",
        @SerializedName("version") var version: String? = "",
        @SerializedName("token") var token: String? = "",
        @SerializedName("batchId") var batchId: Int? = -1,
        @SerializedName("emvData") var emvData: String? = ""
    ) {
        data class BillTo(
            @SerializedName("firstName") var firstName: String? = "",
            @SerializedName("lastName") var lastName: String? = "",
            @SerializedName("address") var address: Address? = Address(),
            @SerializedName("phoneNumber") var phoneNumber: String? = "",
            @SerializedName("emailAddress") var emailAddress: String? = ""
        )
    }

    data class Address(
        @SerializedName("address") var address: String? = "",
        @SerializedName("address2") var address2: String? = "",
        @SerializedName("address3") var address3: String? = "",
        @SerializedName("city") var city: String? = "",
        @SerializedName("state") var state: String? = "",
        @SerializedName("postalCode") var postalCode: String? = "",
        @SerializedName("country") var country: String? = ""
    )
}

/*
URL: https://dev.pacegateway.com/api/terminal/sale
Method: POST

Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzaWQiOiIxMDMzMTI5ODg5OTIzMDc4NCIsInVzZXJJZCI6Ijc3ODMyMjE5NDEwNDMyNjQiLCJoaWVyYXJjaHlJZCI6IjEwMjkwMjg4NTY2ODYxODg4IiwibG9jYWxlIjoiIiwibmVlZF9jaGFuZ2VfcGFzcyI6IjIiLCJuZWVkX2NoYW5nZV9uYW1lIjoiMSIsIm11c3Rfc2VsZWN0X2hpZXJhcmNoeSI6IjAiLCJ1bnZlcmlmaWVkIjoibnVsbCIsIm5iZiI6MTY2MDEzOTExMCwiZXhwIjoxNjkxNjc1MTEwLCJpc3MiOiJQQUNFX0dBVEVXQVlfUE9SVEFMIiwiYXVkIjoiUEFDRV9HQVRFV0FZX1BPUlRBTCJ9.AMxUjZpbrzNoYz7hqDOoSQQEL5BRhaFGvt-hutoxalM
Connection: Keep-Alive
Host: dev.pacegateway.com


{
  "Type": 4,
  "account": {
    "billTo": {
      "address": {
        "address": "",
        "address2": "",
        "address3": "",
        "city": "",
        "country": "",
        "postalCode": "",
        "state": ""
      },
      "emailAddress": "",
      "firstName": "UAT USA",
      "lastName": "UAT USA",
      "phoneNumber": ""
    },
    "expirationDate": "1224",
    "pan": "374245002771003",
    "trackData": "374245002771003\u003d241270215021234500000"
  },
  "amount": 0.23,
  "currencyCode": "USD",
  "shipTo": {
    "address": {
      "address": "",
      "address2": "",
      "address3": "",
      "city": "",
      "country": "",
      "postalCode": "",
      "state": ""
    },
    "emailAddress": "",
    "firstName": "",
    "lastName": "",
    "phoneNumber": ""
  },
  "tipAmount": 0
}
* */