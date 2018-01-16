package br.com.bpelogia.viewcustom.extensions

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * @author Bruno Pelogia < bruno.pelogia@zflow.com.br >
 * @since 10/01/2018
 */

fun String.unmaskMonetary(): String {
    return this.replace("[R$.]".toRegex(), "").replace("[,]".toRegex(), ".")
}

fun String.onlyNumbers(): String {
    return this.replace("[^0-9]*".toRegex(), "")
}

fun String.onlyLetters(): String {
    return this.replace("[^A-Za-z]*".toRegex(), "")
}

fun String.onlyAlphanumerics(): String {
    return this.replace("[^A-Z0-9]*".toRegex(), "")
}

/**
 * Validate CPF.
 *
 * @param cpf String
 * @return true case CPF is valid,
 */
fun String.isCPFValid(): Boolean {
    val cpf = this.onlyNumbers()
    if (cpf == "00000000000" || cpf == "11111111111"
            || cpf == "22222222222" || cpf == "33333333333"
            || cpf == "44444444444" || cpf == "55555555555"
            || cpf == "66666666666" || cpf == "77777777777"
            || cpf == "88888888888" || cpf == "99999999999") {
        return false
    }
    val dig10: Char
    val dig11: Char
    var sm: Int
    var i: Int
    var r: Int
    var num: Int
    var peso: Int
    try {
        sm = 0
        peso = 10
        i = 0
        while (i < 9) {
            num = cpf[i].toInt() - 48
            sm += num * peso
            peso -= 1
            i++
        }
        r = 11 - sm % 11
        dig10 = if (r == 10 || r == 11) '0' else (r + 48).toChar()

        sm = 0
        peso = 11
        i = 0
        while (i < 10) {
            num = cpf[i].toInt() - 48
            sm += num * peso
            peso -= 1
            i++
        }

        r = 11 - sm % 11
        dig11 = if (r == 10 || r == 11) '0' else (r + 48).toChar()
        return dig10 == cpf[9] && dig11 == cpf[10]
    } catch (error: Exception) {
        return false
    }

}

fun String.isDateValid(pattern: String = "dd/MM/yyyy"): Boolean {
    return try {
        val df = SimpleDateFormat(pattern, Locale("pt", "BR"))
        df.isLenient = false
        val dateParsed = df.parse(this)
        val dateValid = GregorianCalendar()
        dateValid.time = dateParsed
        val year = dateValid.get(Calendar.YEAR)
        year > 1500
    } catch (e: ParseException) {
        false
    }

}

fun String.formatCPF(isWithAsterisk: Boolean): String {
    var cpf = this
    var mask = "$1.$2.$3-$4"
    cpf = cpf.replace("(\\D)".toRegex(), "")
    if (isWithAsterisk) {
        mask = "$1.***.***-$4"
    }
    val pattern = Pattern.compile("(\\d{3})(\\d{3})(\\d{3})(\\d{2})")
    val matcher = pattern.matcher(cpf)
    if (matcher.matches())
        cpf = matcher.replaceAll(mask)
    return cpf
}

fun String.formatPhone(isWithAsterisk: Boolean): String {
    var phoneLocal = this
    val patternString = if (phoneLocal.length > 10) "(\\d{2})(\\d{5})(\\d{4})" else "(\\d{2})(\\d{4})(\\d{4})"
    var mask = "($1) $2-$3"

    phoneLocal = phoneLocal.replace("(\\D)".toRegex(), "")
    if (isWithAsterisk) {
        mask = if (phoneLocal.length > 10) "($1) *****-$3" else "($1) ****-$3"
    }

    val pattern = Pattern.compile(patternString)
    val matcher = pattern.matcher(phoneLocal)
    if (matcher.matches())
        phoneLocal = matcher.replaceAll(mask)
    return phoneLocal
}

fun String.formatCEP(isWithAsterisk: Boolean): String {
    var cepLocal = this
    var mask = "$1-$2"
    cepLocal = cepLocal.replace("(\\D)".toRegex(), "")
    if (isWithAsterisk) {
        mask = "*****-$2"
    }
    val pattern = Pattern.compile("(\\d{5})(\\d{3})")
    val matcher = pattern.matcher(cepLocal)
    if (matcher.matches())
        cepLocal = matcher.replaceAll(mask)
    return cepLocal
}

