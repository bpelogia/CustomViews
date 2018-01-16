package br.com.bpelogia.viewcustom.ui

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import br.com.bpelogia.viewcustom.R
import br.com.bpelogia.viewcustom.application.VCAplication
import br.com.bpelogia.viewcustom.extensions.*


/**
 *
 * @author Bruno Pelogia < bruno.pelogia@zflow.com.br>
 * @since 10/01/2018
 */
class CustomMaskEditText @JvmOverloads constructor(context: Context, attr: AttributeSet? = null, mask: String? = "", placeholder: Char = ' ', required: Boolean = false) : AppCompatEditText(context, attr) {

    private var mask: String? = null
    private var placeholder: String? = null
    var isValid = false

    companion object {
        private val MONETARY_MASK = VCAplication.instance.getString(R.string.monetary_mask_format)
        private val CPF_MASK = VCAplication.instance.getString(R.string.cpf_mask_format)
        private val PHONE_MASK = VCAplication.instance.getString(R.string.phone_mask_format)
        private val CELLPHONE_MASK = VCAplication.instance.getString(R.string.cellphone_mask_format)
        private val DATE_MASK = VCAplication.instance.getString(R.string.date_mask_format)
        private val CEP_MASK = VCAplication.instance.getString(R.string.cep_mask_format)
        private val PLATE_MASK = VCAplication.instance.getString(R.string.plate_mask_format)
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
                MONETARY_MASK -> InputType.TYPE_CLASS_NUMBER
                CPF_MASK -> InputType.TYPE_CLASS_NUMBER
                CELLPHONE_MASK -> InputType.TYPE_CLASS_NUMBER
                PHONE_MASK -> InputType.TYPE_CLASS_NUMBER
                DATE_MASK -> InputType.TYPE_CLASS_NUMBER
                CEP_MASK -> InputType.TYPE_CLASS_NUMBER
                PLATE_MASK -> InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                else -> InputType.TYPE_CLASS_TEXT
            }
        }

        val textWatcher: TextWatcher
        if (MONETARY_MASK == mask) {
            textWatcher = object : TextWatcher {
                internal var maskedEditText = editText
                internal var isRequiredField = isRequired

                internal var old = ""

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val qtdDigitsNow = s.toString().onlyNumbers().length
                    val qtdDigitsPermited = mask?.filter { it == '#' }?.length ?: return
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
                            textFormatted = (parsed / 100).formatMoney(true)
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
                        val valueDefault = 0.00.formatMoney(true)
                        val lengthDefault = valueDefault.length
                        old = if (textFormatted == valueDefault) "" else textFormatted

                        selection = if ((selection <= lengthDefault || selection > maskSize) && lengthDefault == maskSize) old.length else selection
                        maskedEditText.setText(old)
                        maskedEditText.setSelection(selection)

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
                    val str = if (PLATE_MASK == mask) s.toString().onlyAlphanumerics() else s.toString().onlyNumbers()

                    if (isUpdating || mask?.length ?: 0 == 0) {
                        old = str
                        isUpdating = false
                        return
                    }
                    val maskWatcher: String? = if ((PHONE_MASK == mask || CELLPHONE_MASK == mask) && str.length >= 3) {
                        when (str[2]) {
                            '9', '8', '7', '6' -> CELLPHONE_MASK
                            else -> PHONE_MASK
                        }
                    } else maskedEditText.mask

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
                    maskedEditText.setText(if (selection == 0 && (PHONE_MASK == mask || CELLPHONE_MASK == mask)) s else mascara)
                    maskedEditText.setSelection(selection)

                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun afterTextChanged(s: Editable) {
                    if (maskedEditText.selectionStart <= 3 && PLATE_MASK == mask && maskedEditText.inputType != InputType.TYPE_CLASS_TEXT) maskedEditText.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                    if (maskedEditText.selectionStart >= 4 && PLATE_MASK == mask && maskedEditText.inputType != InputType.TYPE_CLASS_NUMBER) maskedEditText.inputType = InputType.TYPE_CLASS_NUMBER
                    isValid = maskedEditText.isValidField(isRequiredField, {
                        when {
                            CPF_MASK == mask -> isValidCpfField()
                            CEP_MASK == mask -> isValidCepField()
                            DATE_MASK == mask -> isValidDateField()
                            PLATE_MASK == mask -> isValidPlateField()
                            PHONE_MASK == mask
                                    || CELLPHONE_MASK == mask -> isValidPhoneField()
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
                val message = VCAplication.instance.getString(R.string.required_field)
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
                VCAplication.instance.getString(R.string.cpf_invalid_field),
                this.getText(true).isCPFValid())
    }

    private fun isValidCepField(): Boolean {
        return setupValidation(
                VCAplication.instance.getString(R.string.cep_invalid_field),
                this.getText(true).length == 8)
    }

    private fun isValidPlateField(): Boolean {
        return setupValidation(
                VCAplication.instance.getString(R.string.plate_invalid_field),
                this.getText(true).length == 7)
    }

    private fun isValidDateField(): Boolean {
        return setupValidation(
                VCAplication.instance.getString(R.string.date_invalid_field),
                this.text.toString().isDateValid())
    }

    private fun isValidPhoneField(): Boolean {
        var messageValidation = VCAplication.instance.getString(R.string.phone_invalid_field)
        var validation = this.getText(true).length == 10

        if (this.getText(true).length >= 3) {
            when (this.getText(true)[2]) {
                '9', '8', '7', '6' -> let {
                    messageValidation = VCAplication.instance.getString(R.string.cellphone_invalid_field)
                    validation = this.getText(true).length == 11
                }
            }
        }
        return setupValidation(messageValidation, validation)

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
        } else if (PLATE_MASK == mask) {
            text.toString().onlyAlphanumerics()
        } else if (MONETARY_MASK == mask) {
            text.toString().unmaskMonetary()
        } else {
            text.toString().onlyNumbers()
        }
    }
}