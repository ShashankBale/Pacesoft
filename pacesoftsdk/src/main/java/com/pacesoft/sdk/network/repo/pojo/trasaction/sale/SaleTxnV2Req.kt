package com.pacesoft.sdk.network.repo.pojo.trasaction.sale


import com.google.gson.annotations.SerializedName

data class SaleTxnV2Req(
    @SerializedName("ShipTo") val shipTo: ShipTo?,
    @SerializedName("amountTotal") val amountTotal: String?, //Total amount with two decimal place only, e.g. "31.01", "31" etc
    @SerializedName("instrumentType") val instrumentType: Int?, //For Enter card details manually it will always be 0
    @SerializedName("DigitalWalletType") val digitalWalletType: Int?, //For Enter card details manually it will always be 0
    @SerializedName("account") val account: Account?
) {
    data class ShipTo(
        @SerializedName("FirstName") val firstName: String?, //Name on card split data by space and keep first part here, e.g. "John Watts" -> ["John", "Watts"] -> "John"
        @SerializedName("LastName") val lastName: String?, //Name on card split data by space and keep all except first part here, e.g. "John Watts" -> ["John", "Watts"] -> "Watts"
        @SerializedName("EmailAddress") val emailAddress: String?,
        @SerializedName("PhoneNumber") val phoneNumber: String?, //with country code, e.g. 919996669966
        @SerializedName("Address") val address: Address? = Address(),
    ) {
        data class Address(
            @SerializedName("Address1") val address1: String? = "",  //Street Address, if any
            @SerializedName("Address2") val address2: String? = "", //Apt./Suite, if any
            @SerializedName("PostalCode") val postalCode: String? = "", //ZipCode, if any
            @SerializedName("City") val city: String? = "", //City, if any
            @SerializedName("State") val state: String? = "", //State, if any
            @SerializedName("Country") val country: String? = "" //US
        )
    }

    data class Account(
        @SerializedName("pan") val pan: String?,
        @SerializedName("cvv") val cvv: String?,
        @SerializedName("expirationDate") val expirationDate: String?,
        @SerializedName("trackData") val trackData: String?,
        @SerializedName("emvTlvData") val emvTlvData: String?,
        @SerializedName("emvData") val emvData: EmvData?,
        @SerializedName("encEmvTlvData") val encryptedTlvData: String?,
        @SerializedName("BillTo") val billTo: ShipTo?,
        //"emvTlvData": ""    //String TLV - Unencrypted
        //"encEmvTlvData": "" //String TLV - Encrypted "IVHEX+ENCDATAHEX"
        /*
        @SerializedName("ctlsClearTlvData")
       val plainEmvData: String?
        */
    ) {

        data class EmvData(
            @SerializedName("tag_DF79") val tagDF79: String?,
            @SerializedName("tag_DF78") val tagDF78: String?,
            @SerializedName("tag_4F") val tag4F: String?,
            @SerializedName("tag_50") val tag50: String?,
            @SerializedName("tag_5F20") val tag5F20: String?,
            @SerializedName("tag_5F24") val tag5F24: String?,
            @SerializedName("tag_5F25") val tag5F25: String?,
            @SerializedName("tag_5F2D") val tag5F2D: String?,
            @SerializedName("tag_5F34") val tag5F34: String?,
            @SerializedName("tag_84") val tag84: String?,
            @SerializedName("tag_9F20") val tag9F20: String?,
            @SerializedName("tag_9F39") val tag9F39: String?,
            @SerializedName("tag_DFEE23") val tagDFEE23: String?,
            @SerializedName("tag_DFEE25") val tagDFEE25: String?,
            @SerializedName("tag_DFEE26") val tagDFEE26: String?,
            @SerializedName("tag_DFEF4C") val tagDFEF4C: String?,
            @SerializedName("tag_DFEF4D") val tagDFEF4D: String?,
            @SerializedName("tag_FFEE01") val tagFFEE01: String?,
            @SerializedName("tag_FFEE12") val tagFFEE12: String?,
            @SerializedName("tag_FFF2") val tagFFF2: String?,
            @SerializedName("tag_57") val tag57: String?,
            @SerializedName("tag_5A") val tag5A: String?,
            @SerializedName("tag_95") val tag95: String?,
            @SerializedName("tag_9B") val tag9B: String?,
            @SerializedName("tag_9F02") val tag9F02: String?,
            @SerializedName("tag_9F03") val tag9F03: String?,
            @SerializedName("tag_9F10") val tag9F10: String?,
            @SerializedName("tag_9F13") val tag9F13: String?,
            @SerializedName("tag_9F26") val tag9F26: String?,
            @SerializedName("tag_9F27") val tag9F27: String?,
            @SerializedName("tag_9F34") val tag9F34: String?,
            @SerializedName("tag_9F36") val tag9F36: String?,
            @SerializedName("tag_9F37") val tag9F37: String?,
            @SerializedName("tag_9F4D") val tag9F4D: String?,
            @SerializedName("tag_9F4F") val tag9F4F: String?,
            @SerializedName("tag_13") val tag13: String?,
            @SerializedName("tag_5F2A") val tag5F2A: String?,
            @SerializedName("tag_82") val tag82: String?,
            @SerializedName("tag_9A") val tag9A: String?,
            @SerializedName("tag_9C") val tag9C: String?,
            @SerializedName("tag_9F06") val tag9F06: String?,
            @SerializedName("tag_9F12") val tag9F12: String?,
            @SerializedName("tag_9F1A") val tag9F1A: String?,
            @SerializedName("tag_9F21") val tag9F21: String?,
            @SerializedName("tag_9F33") val tag9F33: String?,
            @SerializedName("tag_9F35") val tag9F35: String?,
            @SerializedName("tag_9F40") val tag9F40: String?,
            @SerializedName("tag_DFEE1A") val tagDFEE1A: String?
        )
    }

    companion object {
        fun getObjForManualCardTxn(
            fullName: String,
            phoneNumber: String,

            addressLine1: String,
            addressLine2: String,
            addressPostalCode: String,
            addressCity: String,
            addressState: String,
            addressCountry: String,

            amountTotal: String,

            cardNo: String,
            cvv: String,
            expirationDate: String,
        ): SaleTxnV2Req {
            var fn = ""
            var ln = ""
            if (fullName.contains(" ")) {
                val fullNameFirstSpaceIndex = fullName.indexOf(" ")
                fn = fullName.substring(0, fullNameFirstSpaceIndex)
                ln = fullName.substring(fullNameFirstSpaceIndex + 1)
            } else {
                fn = fullName
            }

            val address = ShipTo.Address(
                address1 = addressLine1,
                address2 = addressLine2,
                postalCode = addressPostalCode,
                city = addressCity,
                state = addressState,
                country = addressCountry,
            )

            val billTo = ShipTo(
                firstName = fn,
                lastName = ln,
                emailAddress = "",
                phoneNumber = phoneNumber,
                address = address,
            )

            val account = Account(
                pan = cardNo, //Card Number, numbers only 16 to 19 digit
                cvv = cvv, //CVV, numbers only 3 digit
                expirationDate = expirationDate, //expiration date in ddmm
                trackData = null, //null always for Enter card details manually
                emvTlvData = null, //null always for Enter card details manually
                emvData = null, //null always for Enter card details manually
                encryptedTlvData = null,
                billTo = billTo,
            )


            val req = SaleTxnV2Req(
                amountTotal = amountTotal,
                instrumentType = 0,
                digitalWalletType = 0,
                shipTo = null,
                account = account,
            )
            return req
        }
    }
}