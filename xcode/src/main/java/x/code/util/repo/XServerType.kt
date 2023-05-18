package x.code.util.repo

enum class XServerType(val index: Int) {
    DEV_SERVER(0),
    QA_SERVER(1),
    UAT_SERVER(2),
    PROD_SERVER(3);


    companion object {
        val aServerTypeNames = arrayOf("DEV", "QA", "UAT", "PROD")

        fun getEnum(num: Int?): XServerType {
            enumValues<XServerType>().forEach {
                if (it.index == num)
                    return it
            }
            return PROD_SERVER
        }

        fun getServerLabel(e: XServerType) = aServerTypeNames[e.index]
    }
}