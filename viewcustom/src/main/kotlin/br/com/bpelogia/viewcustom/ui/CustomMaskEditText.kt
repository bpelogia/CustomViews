package br.com.bpelogia.viewcustom.ui

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import br.com.bpelogia.viewcustom.R
import br.com.bpelogia.viewcustom.extensions.*


/**
 *
 * @author Bruno Pelogia
 * @since 10/01/2018
 */
class CustomMaskEditText @JvmOverloads constructor(context: Context, attr: AttributeSet? = null, mask: String? = "", placeholder: Char = ' ', required: Boolean = false) : AppCompatEditText(context, attr) {

    private var mask: String? = null
    private var placeholder: String? = null
    private var isLandLineAndMobile = false

    fun setIsLandLineAndMobile(value :Boolean) { isLandLineAndMobile = value }

    var isValid = false
        private set



    companion object {
        private val MONETARY_MASK = R.string.monetary_mask_format
        private val CPF_MASK = R.string.cpf_mask_format
        private val PHONE_MASK = R.string.phone_mask_format
        private val CELLPHONE_MASK = R.string.cellphone_mask_format
        private val DATE_MASK = R.string.date_mask_format
        private val CEP_MASK = R.string.cep_mask_format
        private val PLATE_MASK = R.string.plate_mask_format
    }

    init {
        if (isInEditMode.not()) {
            var maskReceived = mask
            var placeholderReceived = placeholder
            var requiredReceived = required
            val a = context.obtainStyledAttributes(attr, R.styleable.CustomMaskEditText)
            val count = a.indexCount

            (0 until count)
                    .asSequence()
                    .map { a.getIndex(it) }
                    .forEach {
                        when (it) {
                            R.styleable.CustomMaskEditText_required -> requiredReceived = a.getBoolean(it, false)
                            R.styleable.CustomMaskEditText_mask -> maskReceived = (if (mask?.length ?: 0 > 0) mask else a.getString(it))
                            R.styleable.CustomMaskEditText_placeholder -> placeholderReceived = (if (a.getString(it).isNotEmpty() && placeholder == ' ') a.getString(it)[0] else placeholder)
                        }
                    }

            a.recycle()

            this.mask = maskReceived
            this.placeholder = placeholderReceived.toString()

            addTextChangedListener(mask(this, requiredReceived))
        }
    }

    private fun mask(editText: CustomMaskEditText, isRequired: Boolean): TextWatcher {
        val mask = editText.getMask()

        if (mask != null) {
            this.inputType = when (mask) {
                MONETARY_MASK.getString(context) -> InputType.TYPE_CLASS_NUMBER
                CPF_MASK.getString(context) -> InputType.TYPE_CLASS_NUMBER
                CELLPHONE_MASK.getString(context) -> InputType.TYPE_CLASS_NUMBER
                PHONE_MASK.getString(context) -> InputType.TYPE_CLASS_NUMBER
                DATE_MASK.getString(context) -> InputType.TYPE_CLASS_NUMBER
                CEP_MASK.getString(context) -> InputType.TYPE_CLASS_NUMBER
                PLATE_MASK.getString(context) -> InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                else -> InputType.TYPE_CLASS_TEXT
            }
        }

        val textWatcher: TextWatcher
        if (MONETARY_MASK.getString(context) == mask) {
            textWatcher = object : TextWatcher {
                internal var maskedEditText = editText
                internal var isRequiredField = isRequired
                internal val hasSymbol = true
                internal var old = ""

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val qtdDigitsNow = s.toString().onlyNumbers().length
                    val qtdDigitsPermited = mask.filter { it == '#' }.length
                    if (qtdDigitsNow > qtdDigitsPermited) {
                        maskedEditText.setText(old)
                        maskedEditText.setSelection(old.length)
                        return
                    }
                    if (!s.toString().isEmpty() && s.toString() != old) {
                        var textFormatted: String = old
                        maskedEditText.removeTextChangedListener(this)
                        val cleanString = s.toString().onlyNumbers()
                        val parsed: Double
                        try {
                            parsed = java.lang.Double.parseDouble(cleanString)
                            textFormatted = (parsed / 100).formatMoney(hasSymbol)
                        } catch (e: NumberFormatException) {
                        }

                        val maskSize = textFormatted.length

                        var selection = if (before == 1) { // Se usuário estiver apagando
                            if (maskedEditText.selectionStart <= maskSize)
                                if (old.length - textFormatted.length >= 2) {
                                    maskedEditText.selectionStart - 1
                                } else maskedEditText.selectionStart
                            else maskSize
                        } else { // Se usuário estiver digitando
                            if (start + (textFormatted.length - old.length) <= maskSize) {
                                start + (textFormatted.length - old.length)
                            } else maskSize
                        }
                        val valueDefault = 0.00.formatMoney(hasSymbol)
                        val lengthDefault = valueDefault.length
                        old = if (textFormatted == valueDefault) "" else textFormatted

                        selection = if ((selection <= lengthDefault && lengthDefault == maskSize) || selection > old.length) old.length else selection
                        maskedEditText.setText(old)
                        maskedEditText.setSelection(if(selection < 0) 0 else if(selection > maskedEditText.text.length) maskedEditText.text.length else selection )

                        maskedEditText.addTextChangedListener(this)
                    }
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun afterTextChanged(s: Editable) {
                    isValid = maskedEditText.isValidField(isRequiredField)
                }
            }

        } else {
            textWatcher = object : TextWatcher {
                internal var maskedEditText = editText
                internal var isRequiredField = isRequired
                internal var isUpdating: Boolean = false
                internal var old = ""

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val str = if (PLATE_MASK.getString(context) == mask) s.toString().onlyAlphanumerics() else s.toString().onlyNumbers()

                    if (isUpdating || mask?.length ?: 0 == 0) {
                        old = str
                        isUpdating = false
                        return
                    }
                    val maskWatcher: String? = if (isLandLineAndMobile && str.length >= 3) {
                        when (str[2]) {
                            '9', '8', '7', '6' -> CELLPHONE_MASK.getString(context)
                            else -> PHONE_MASK.getString(context)
                        }
                    } else maskedEditText.mask

                    maskedEditText.mask = maskWatcher

                    isUpdating = true
                    var selection = 0
                    var mascara = maskWatcher?.let { mask(maskWatcher, str) }
                    if (mascara?.isNotEmpty() == true) {
                        var lastChar = mascara[mascara.length - 1]
                        var hadSign = false

                        while (isASign(lastChar)
                                && mascara?.isNotEmpty() == true
                                && str.isNotEmpty()
                                && str.length == old.length) {

                            mascara = mascara.substring(0, mascara.length - 1)
                            lastChar = mascara[mascara.length - 1]
                            hadSign = true
                        }

                        if (mascara?.isNotEmpty() == true && hadSign) {
                            mascara = mascara.substring(0, mascara.length - 1)
                        }

                        val maskSize = mascara?.length ?: start

                        selection = if (before == 1) { // Se usuário estiver apagando
                            if (maskedEditText.selectionStart > maskSize) {
                                maskedEditText.selectionStart - (maskedEditText.selectionStart - maskSize)
                            } else {
                                maskedEditText.selectionStart
                            }
                        } else { // Se usuário estiver digitando
                            if ((maskSize - maskedEditText.selectionStart) > count) {
                                if (isASign(mascara!![maskedEditText.selectionStart])) {
                                    maskedEditText.selectionStart + 1
                                } else {
                                    maskedEditText.selectionStart
                                }
                            } else maskSize
                        }
                    }
                    if (selection <= 3 && PLATE_MASK.getString(context) == mask && maskedEditText.inputType != InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) maskedEditText.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                    if (selection > 3 && PLATE_MASK.getString(context) == mask && maskedEditText.inputType != InputType.TYPE_CLASS_NUMBER) maskedEditText.inputType = InputType.TYPE_CLASS_NUMBER
                    maskedEditText.setText(if (selection == 0 && (PHONE_MASK.getString(context) == mask || CELLPHONE_MASK.getString(context) == mask)) s else mascara)
                    maskedEditText.setSelection(selection)

                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun afterTextChanged(s: Editable) {

                    isValid = maskedEditText.isValidField(isRequiredField, {
                        when (mask) {
                            CPF_MASK.getString(context) -> isValidCpfField()
                            CEP_MASK.getString(context) -> isValidCepField()
                            DATE_MASK.getString(context) -> isValidDateField()
                            PLATE_MASK.getString(context) -> isValidPlateField()
                            PHONE_MASK.getString(context), CELLPHONE_MASK.getString(context) -> isValidPhoneField()
                            else -> true
                        }
                    })
                }
            }
        }

        return textWatcher
    }

    fun mask(mask: String, text: String): String {
        var i = 0
        var mascara = ""
        for (m in mask.toCharArray()) {
            if (m != '#' && m != 'A') {
                mascara += m
                continue
            }
            try {
                mascara += if (m == 'A' && !text[i].isDigit()
                        || m == '#' && text[i].isDigit()) text[i] else ""
            } catch (e: Exception) {
                break
            }
            i++
        }



        return mascara
    }

    /**
     * Valida campo com mensagem caso esteja vazio ou monetário 0,00
     *
     * @return true se campo nao for vazio ou zero, false se conter zero ou for vazio.
     */
    fun isValidField(isRequiredField: Boolean, block: (() -> Boolean)? = null): Boolean {
        val textInputLayout = this.parent.parent as TextInputLayout

        if (this.isEmptyFieldOrZero()) {
            if (isRequiredField) {
                val message = R.string.required_field.getString(context)
                textInputLayout.isErrorEnabled = true
                textInputLayout.error = message
                this.requestFocus()
                return false
            }
        }
        textInputLayout.isErrorEnabled = false
        textInputLayout.error = null
        return block?.invoke() ?: true
    }

    private fun isValidCpfField(): Boolean {
        return setupValidation(
                R.string.cpf_invalid_field.getString(context),
                this.getText(true).isCPFValid())
    }

    private fun isValidCepField(): Boolean {
        return setupValidation(
                R.string.cep_invalid_field.getString(context),
                this.getText(true).length == 8)
    }

    private fun isValidPlateField(): Boolean {
        return setupValidation(
                R.string.plate_invalid_field.getString(context),
                this.getText(true).length == 7)
    }

    private fun isValidDateField(): Boolean {
        return setupValidation(
                R.string.date_invalid_field.getString(context),
                this.text.toString().isDateValid())
    }

    private fun isValidPhoneField(): Boolean {
        val messageValidation = if(mask == PHONE_MASK.getString(context))
                                    R.string.phone_invalid_field.getString(context)
                                else R.string.cellphone_invalid_field.getString(context)

        val validDigit = if (this.getText(true).length >= 3) {
            when (this.getText(true)[2]) {
                '9', '8', '7', '6' -> mask == CELLPHONE_MASK.getString(context)
                else -> mask == PHONE_MASK.getString(context)
            }
        } else true

        val validation = if(mask == PHONE_MASK.getString(context))
                            this.getText(true).length == 10
                        else this.getText(true).length == 11

        return setupValidation(messageValidation, validation && validDigit)

    }

    private fun setupValidation(message: String, validation: Boolean): Boolean {
        val ti = this.parent.parent as TextInputLayout
        if (!validation && this.isEmptyFieldOrZero().not()) {
            ti.error = message
            this.requestFocus()
        } else {
            ti.error = null
        }
        ti.isErrorEnabled = !validation && this.isEmptyFieldOrZero().not()
        return validation
    }


    fun isASign(c: Char): Boolean {
        return c == '.' || c == '-' || c == '/' || c == '(' || c == ')' || c == ',' || c == ' '
    }

    private fun getMask(): String? {
        return mask
    }

    fun setMask(mask: String) {
        this.mask = mask
        text = text
    }

    fun getPlaceholder(): Char {
        return placeholder?.let { placeholder!![0] } ?: ' '
    }

    fun setPlaceholder(placeholder: Char) {
        this.placeholder = placeholder.toString()
        text = text
    }

    fun getText(removeMask: Boolean = false): String {
        return if (!removeMask) {
            text.toString()
        } else if (PLATE_MASK.getString(context) == mask) {
            text.toString().onlyAlphanumerics()
        } else if (MONETARY_MASK.getString(context) == mask) {
            text.toString().unmaskMonetary()
        } else {
            text.toString().onlyNumbers()
        }
    }
}