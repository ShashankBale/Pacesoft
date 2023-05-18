package x.code.util.view

import androidx.appcompat.app.AppCompatDelegate

object XTheme {

    /*Don't change this number, because it been saved in sharedPreference*/
    enum class Mode(val num: Int) {
        Default(0),
        Light(1),
        Dark(2);

        companion object {
            fun getEnum(num: Int?): Mode {
                enumValues<Mode>().forEach {
                    if (it.num == num)
                        return it
                }
                return Default
            }
        }
    }

    fun applyTheme(mode: Mode) {
        when (mode) {
            Mode.Default -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            Mode.Light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Mode.Dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            //batterySaverMode -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
    }

    fun tempApplyTheme(mode: Mode) {
        when (mode) {
            Mode.Default -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Mode.Light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Mode.Dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            //batterySaverMode -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
    }
}