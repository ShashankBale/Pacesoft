package x.code.util

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object XCoroutines {

    val mCeh = CoroutineExceptionHandler { _, t -> t.printStackTrace() }

    fun io(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO + mCeh).launch {
            work()
        }

    fun default(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Default + mCeh).launch {
            work()
        }

    fun main(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Main + mCeh).launch {
            work()
        }
}