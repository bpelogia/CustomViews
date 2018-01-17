package br.com.bpelogia.viewcustom.extensions

import android.widget.EditText

/**
 * @author Bruno Pelogia
 * @since 10/01/2018
 */

/**
 * Valida campo vazio ou monetário com 0,00
 *
 * @return true vazio ou zero, false preenchido ou diferente de zero
 */
fun EditText.isEmptyFieldOrZero(): Boolean {
    return this.text.toString().trim { it <= ' ' }.isEmpty() || isZeroField()
}

/**
 * Valida campo monetário com 0,00
 *
 * @return true se zero, false se nao conter zero
 */
fun EditText.isZeroField(): Boolean {
    return "0.00" == this.text.toString().trim { it <= ' ' }.unmaskMonetary()
}
