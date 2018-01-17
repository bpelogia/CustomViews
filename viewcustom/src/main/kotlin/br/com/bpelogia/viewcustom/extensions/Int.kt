package br.com.bpelogia.viewcustom.extensions

import android.content.Context

/**
 * Created by Bruno Pelogia
 * @since 17/01/2018.
 */
fun Int.getString(context : Context): String {
    return context.getString(this)
}