package com.pacesoft.sdk.network.repo.pojo.user.merchant.profile

class MProfileSessionDataDetails(
    val id: Int,
    val location: String,
    //val type: String,
    val date: String,
    val time: String,
    val action: String,
) {
    companion object {
        fun getDummy(): List<MProfileSessionDataDetails> {

            return listOf(
                MProfileSessionDataDetails(
                    1,
                    "Detroit, MI 48317",
                    "03/08/2023",
                    "04:30:00 PM",
                    "Login"
                ),
                MProfileSessionDataDetails(
                    1,
                    "Detroit, MI 48317",
                    "03/08/2023",
                    "04:05:00 PM",
                    "App Launched"
                ),
                MProfileSessionDataDetails(
                    1,
                    "Detroit, MI 48317",
                    "01/07/2022",
                    "03:00:00 PM",
                    "App Closed"
                ),
                MProfileSessionDataDetails(
                    1,
                    "Detroit, MI 48317",
                    "03/08/2023",
                    "09:00:00 AM",
                    "Refund",
                ),
                MProfileSessionDataDetails(
                    1,
                    "Detroit, MI 48317",
                    "03/08/2023",
                    "07:00:00 AM",
                    "Sale"
                ),
                MProfileSessionDataDetails(
                    1,
                    "Detroit, MI 48317",
                    "05/02/2022",
                    "05:00:00 PM",
                    "App Launched"
                ),
                MProfileSessionDataDetails(
                    1,
                    "Detroit, MI 48317",
                    "03/08/2023",
                    "09:00:45 PM",
                    "Login"
                ),
                MProfileSessionDataDetails(
                    1,
                    "Detroit, MI 48317",
                    "03/08/2023",
                    "09:00:45 PM",
                    "Login"
                ),

                )
        }
    }
}