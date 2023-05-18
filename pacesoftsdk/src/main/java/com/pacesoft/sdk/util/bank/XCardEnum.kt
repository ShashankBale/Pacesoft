package com.pacesoft.sdk.util.bank

import android.os.Parcelable
import com.pacesoft.sdk.R
import kotlinx.android.parcel.Parcelize


@Parcelize
enum class XCardEnum(val cardIcon: Int) : Parcelable {
    Amex(R.drawable.img_card_american_express),
    Mastercard(R.drawable.img_card_master),
    Visa(R.drawable.img_card_visa),
    Discover(R.drawable.img_card_discover),
    Other(R.drawable.img_placeholder_default_card_2);


    /*
    Other = 0,

    AmericanExpress = 3,
    Amex = AmericanExpress,
    AmericanExpressDebit = AmericanExpress,

    PayPal = 5,

    CUP = 6,

    JCB = 7,

    Visa = 1,
    VisaElectron = Visa,
    VisaInterlink = Visa,
    VisaCredit = Visa,
    VisaDebit = Visa,

    Mastercard = 2,
    Maestro = Mastercard,
    MasterCardCredit = Mastercard,
    MasterCardDebit = Mastercard,
    USMaestro = Mastercard,

    Discover = 4,
    ContactlessDPAS = Discover,
    DiscoverDebit = Discover,
    Diners = Discover
    * */

    companion object {
        fun getEnumFromCardBrand(cardBrand: String?): XCardEnum {
            return cardBrand?.lowercase().let {
                when (it) {
                    "americanexpress",
                    "amex",
                    "americanexpressdebit" -> Amex

                    "visa",
                    "visaelectron",
                    "visainterlink",
                    "visacredit",
                    "visadebit" -> Visa

                    "mastercard",
                    "maestro",
                    "mastercardcredit",
                    "mastercarddebit",
                    "usmaestro" -> Mastercard

                    "discover",
                    "contactlessdpas",
                    "discoverdebit",
                    "diners" -> Discover

                    else -> Other
                }
            }
        }

        fun getCardIconFromCardBrand(cardBrand: String?): Int {
            return getEnumFromCardBrand(cardBrand).cardIcon
        }

        fun getEnumFromCardNo(cardNo: String): XCardEnum {
            val ptVisa = Regex("^4[0-9]{6,}$")
            if (cardNo.matches(ptVisa)) return Visa

            val ptMasterCard = Regex("^5[1-5][0-9]{5,}$")
            if (cardNo.matches(ptMasterCard)) return Mastercard

            val ptAmeExp = Regex("^3[47][0-9]{5,}$")
            if (cardNo.matches(ptAmeExp)) return Amex

            val ptDiscover = Regex("^6(?:011|5[0-9]{2})[0-9]{3,}$")
            if (cardNo.matches(ptDiscover)) return Discover

            /*
            val ptDinClb = Regex("^3(?:0[0-5]|[68][0-9])[0-9]{4,}$")
            if (cardNo.matches(ptDinClb)) return Visa

            val ptJcb = Regex("^(?:2131|1800|35[0-9]{3})[0-9]{3,}$")
            if (cardNo.matches(ptJcb)) return Visa
            */

            return Other
        }

    }
}