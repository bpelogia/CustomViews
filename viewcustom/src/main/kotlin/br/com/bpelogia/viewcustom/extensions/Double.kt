package br.com.bpelogia.viewcustom.extensions

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

/**
 * @author Bruno Pelogia
 * @since 10/01/2018
 */

/**
 * Responsável por formatar um número double em
 * um número monetário com ou sem Símbolo
 *
 * @param withSymbol - true/false
 * @return - número monetário com ou sem símbolo
 */
fun Double.formatMoney(withSymbol: Boolean): String {
    val ptBr = Locale("pt", "BR")
    val nf = NumberFormat.getCurrencyInstance(ptBr)
    val doubleFormatted = nf.format(this)
    return if(withSymbol) doubleFormatted else doubleFormatted.replace("R$", "").trim { it <= ' ' }
}

fun Double.formatThousand(label: String?=""): String {
    val df = DecimalFormat("#,###,###,###", DecimalFormatSymbols(Locale("pt", "BR")))
    return df.format(this).plus(label)
}