package com.rexmtorres.cmp

import android.content.Context
import java.lang.ref.WeakReference

actual object AppContext {
    private var value: WeakReference<Context?>? = null

    fun set(context: Context) {
        value = WeakReference(context)
    }

    actual fun get(): Any = value?.get() ?: throw RuntimeException("Missing context!")
}
