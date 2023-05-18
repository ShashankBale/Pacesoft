package x.code.util

object XBuild {
    private const val production = false
    private const val betaTesting = false
    private const val alphaTesting = false
    private const val internalTesting = true
    private const val debug = true

    fun isDebug() = debug
    fun isInternalTesting() = internalTesting || debug
    fun isAlphaTesting() = alphaTesting || internalTesting || debug
    fun isBetaTesting() = betaTesting || alphaTesting || internalTesting || debug

}