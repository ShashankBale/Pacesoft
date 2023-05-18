package x.code.util.pojo

import x.code.util.convert
import javax.security.auth.login.LoginException

data class SecurityHelperResult(val errorMsg:String?, val encryptedData : String, val decryptedData :String?) {
    fun toBaseRequest(): BaseRequest {
        return BaseRequest(message = encryptedData)
    }

   /* fun <T> toResponse(reified O) {
            decryptedData.convert<String,>()
    }*/

//    {message = "sjadfkjsdkaljflksdajfkl"}
}