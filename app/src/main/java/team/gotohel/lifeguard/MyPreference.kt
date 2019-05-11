package team.gotohel.lifeguard

import android.content.Context

object MyPreference  {

    private const val APPLICATION_PREFS_NAME = "preference_application"
    private fun getAppPreference() = MyApplication.context.getSharedPreferences(APPLICATION_PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Login Preference
     */
    private const val keyAllergenList = "keyAllergenList"
    var savedAllergenList: List<String>?
        get() = getAppPreference().getString(keyAllergenList, null)?.split(",")
        set(value) = getAppPreference().edit()
            .putString(keyAllergenList, if (value?.isNotEmpty() == true) value.joinToString(",") else null)
            .apply()

    fun addAllergen(newAllergen: String) {
        val oldList = savedAllergenList ?: listOf()
        savedAllergenList = oldList + newAllergen
    }


}