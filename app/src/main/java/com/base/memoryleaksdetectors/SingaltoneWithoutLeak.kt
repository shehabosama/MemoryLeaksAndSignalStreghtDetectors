package com.base.memoryleaksdetectors

import android.content.Context
import java.lang.ref.WeakReference

class SingaltoneWithoutLeak  private constructor(private val context: WeakReference<Context>) {
    private val TAG = "MyApp"
    companion object {
        private var ourInstance: SingaltoneWithoutLeak? = null

        fun getInstance(context: Context): SingaltoneWithoutLeak? {
            if (ourInstance == null) {
                ourInstance = SingaltoneWithoutLeak(WeakReference(context))
            }
            return ourInstance
        }
    }
}