package com.pacesoft.sdk.network.repo.pojo.user.merchant

import android.os.Parcelable
import com.pacesoft.sdk.R
import kotlinx.android.parcel.Parcelize

@Parcelize
class MReportCardTxnDetails(
    val id: Int,
    val logo: Int,
    val merchantName: String,
    val type: String,
    val cardNo: String,
    val datetime: String,
    val amount: String,
    val status: String,
) : Parcelable {
    companion object {
        fun getDummy(): List<MReportCardTxnDetails> {
            return listOf(
                MReportCardTxnDetails(
                    1,
                    R.drawable.img_card_discover,
                    "David Leppek",
                    "Sale",
                    "..2222",
                    "06/02/2022 09:00:45 PM",
                    "\$4.53",
                    "Approved"
                ),
                MReportCardTxnDetails(
                    1,
                    R.drawable.img_card_american_express,
                    "Todd Paynter",
                    "Void",
                    "..1451",
                    "06/02/2022 09:00:45 PM",
                    "\$23.12",
                    "Approved"
                ),
                MReportCardTxnDetails(
                    1,
                    R.drawable.img_card_visa,
                    "David Jennings",
                    "Void",
                    "..2314",
                    "01/07/2022 09:00:45 PM",
                    "\$22.11",
                    "Approved"
                ),
                MReportCardTxnDetails(
                    1,
                    R.drawable.img_card_visa,
                    "Shashank Bale",
                    "Sale",
                    "..1111",
                    "06/02/2022 09:00:45 PM",
                    "\$16.33",
                    "Approved",
                ),
                MReportCardTxnDetails(
                    1,
                    R.drawable.img_card_master,
                    "Ganesh Choudhari",
                    "Refund",
                    "..5678",
                    "06/02/2022 09:00:45 PM",
                    "\$100.27",
                    "Declined"
                ),
                MReportCardTxnDetails(
                    1,
                    R.drawable.img_card_discover,
                    "Amruta",
                    "Sale",
                    "..2222",
                    "06/02/2022 09:00:45 PM",
                    "\$4.53",
                    "Approved"
                ),
                MReportCardTxnDetails(
                    1,
                    R.drawable.img_card_american_express,
                    "Uttam Shah",
                    "Void",
                    "..1451",
                    "06/02/2022 09:00:45 PM",
                    "\$25.03",
                    "Declined"
                ),
                MReportCardTxnDetails(
                    1,
                    R.drawable.img_card_master,
                    "Rekhit Rathore",
                    "Refund",
                    "..5678",
                    "06/02/2022 09:00:45 PM",
                    "\$100.27",
                    "Declined"
                ),

                )
        }
    }
}