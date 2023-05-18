package com.pacesoft.sdk.network.repo.pojo.user.merchant

data class NearByCustomerDetail(
    val customerMobile: String,
    val beaconName: String,
    val beaconRange: String,
) {
    companion object {
        fun getDummy(): List<NearByCustomerDetail> {

            return listOf(
                NearByCustomerDetail( "516-489-6523", "Beacon 1", "-24dBm"),
                NearByCustomerDetail( "123-489-6523", "Beacon 2", "-40dBm"),
                NearByCustomerDetail( "456-489-6523", "Beacon 3", "-42dBm"),
                NearByCustomerDetail( "790-489-6523", "Beacon 4", "-59dBm"),
                NearByCustomerDetail( "321-489-6523", "Beacon 5", "-70dBm"),
                NearByCustomerDetail( "654-489-6523", "Beacon 6", "-82dBm"),
                NearByCustomerDetail( "951-489-6523", "Beacon 7", "-90dBm"),
            )
        }
    }

}