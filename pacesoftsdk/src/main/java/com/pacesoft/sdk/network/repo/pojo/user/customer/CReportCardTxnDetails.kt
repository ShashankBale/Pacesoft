package com.pacesoft.sdk.network.repo.pojo.user.customer

import com.pacesoft.sdk.R

class CReportCardTxnDetails(
    val id: Int,
    val logo: Int,
    val merchantName: String,
    val type: String,
    val cardNo: String,
    val datetime: String,
    val amount: String,
    val status: String,
) {
    companion object {
        fun getDummy(): List<CReportCardTxnDetails> {

            return listOf(
                CReportCardTxnDetails(
                    1,
                    R.drawable.img_card_visa,
                    "Mcdonalds",
                    "Sale",
                    "..1111",
                    "06/02/2022 09:00:45 PM",
                    "\$16.33",
                    "Approved",
                ),
                CReportCardTxnDetails(
                    1,
                    R.drawable.img_card_master,
                    "David’s Coffee Shop",
                    "Refund",
                    "..5678",
                    "06/02/2022 09:00:45 PM",
                    "\$100.27",
                    "Declined"
                ),
                CReportCardTxnDetails(
                    1,
                    R.drawable.img_card_discover,
                    "ABC Learning",
                    "Sale",
                    "..2222",
                    "06/02/2022 09:00:45 PM",
                    "\$4.53",
                    "Approved"
                ),
                CReportCardTxnDetails(
                    1,
                    R.drawable.img_card_american_express,
                    "Bill’s Brews and Chews",
                    "Void",
                    "..1451",
                    "06/02/2022 09:00:45 PM",
                    "\$25.03",
                    "Declined"
                ),
                CReportCardTxnDetails(
                    1,
                    R.drawable.img_card_master,
                    "David’s Coffee Shop",
                    "Refund",
                    "..5678",
                    "06/02/2022 09:00:45 PM",
                    "\$100.27",
                    "Declined"
                ),
                CReportCardTxnDetails(
                    1,
                    R.drawable.img_card_discover,
                    "ABC Learning",
                    "Sale",
                    "..2222",
                    "06/02/2022 09:00:45 PM",
                    "\$4.53",
                    "Approved"
                ),
                CReportCardTxnDetails(
                    1,
                    R.drawable.img_card_american_express,
                    "Bill’s Brews and Chews",
                    "Void",
                    "..1451",
                    "06/02/2022 09:00:45 PM",
                    "\$23.12",
                    "Declined"
                )
            )
        }
    }
}