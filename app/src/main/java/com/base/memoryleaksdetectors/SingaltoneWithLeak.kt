package com.base.memoryleaksdetectors

import android.content.Context

class SingaltoneWithLeak private constructor(private val context: Context) {

    companion object {

        private var ourInstance: SingaltoneWithLeak? = null

        fun getInstance(context: Context): SingaltoneWithLeak? {
            if (ourInstance == null) {
                ourInstance = SingaltoneWithLeak(context)
            }
            return ourInstance
        }
    }
}