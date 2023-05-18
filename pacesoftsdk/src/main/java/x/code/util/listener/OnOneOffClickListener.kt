package x.code.util.listener

import android.view.View

abstract class OnOneOffClickListener : View.OnClickListener {
    private var clickable = true
    override fun onClick(v: View) {
        if (clickable) {
            clickable = false
            onOneClick(v)
            //reset(); // uncomment this line to reset automatically
        }
    }

    abstract fun onOneClick(v: View?)
    fun reset() {
        clickable = true
    }
}