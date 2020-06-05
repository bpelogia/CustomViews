package br.com.bpelogia.appteste.flow.dashboard

import android.os.Bundle
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.bpelogia.appteste.R
import br.com.bpelogia.viewcustom.extensions.formatCNPJ
import br.com.bpelogia.viewcustom.extensions.formatMoney
import br.com.bpelogia.viewcustom.extensions.moveDownViewOnScrolling
import br.com.bpelogia.viewcustom.ui.CustomMaskEditText
import br.com.bpelogia.viewcustom.ui.CustomNumberPicker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by Bruno Pelogia
 * @since 27/12/2017
 */
class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun validarCampos() {
        imprimir("CPF", et_cpf)
        imprimir("CNPJ", et_cnpj)
        imprimir("Placa", et_plate)
        imprimir("Valor", et_monetary)
        imprimir("Cep", et_cep)
        imprimir("Data", et_date)
        imprimir("Telefone", et_phone)
        imprimir("Normal", et_normal)
    }

    fun imprimir(fieldName: String, field: CustomMaskEditText) {
        val message = if (field.isValid) "$fieldName Válido! - ${field.text}" else "$fieldName INVÁLIDO!! - ${field.text}"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        scroll_view.moveDownViewOnScrolling(navigation)

        //Validar campos
        bt_validate.setOnClickListener { validarCampos() }

        //Campo telefone/celular
        et_phone.setIsLandLineAndMobile(true)

        //Acrescentando Validacao
        et_monetary.setOnValidationListener(object : CustomMaskEditText.OnValidationListener {
            override fun doOnTextChange(view: CustomMaskEditText) {
                Toast.makeText(this@MainActivity, "valor Digitado: ${view.getText(false)}", Toast.LENGTH_SHORT).show()
            }

            override fun doOnAfterTextChange(view: CustomMaskEditText): Boolean {
                val valid = view.getText(false) == getValorFormatado(picker.value)
                val message = "valor inválido!"
                return validField(view,valid, message)
            }

        })

        et_cnpj.setOnValidationListener(object : CustomMaskEditText.OnValidationListener {

            override fun doOnAfterTextChange(view: CustomMaskEditText): Boolean {
                Toast.makeText(this@MainActivity, "CNPJ: ${view.getText(true).formatCNPJ(false)}", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun doOnTextChange(view: CustomMaskEditText) {
            }
        })

        setupCustomNumberPicker()
    }

    private fun validField(view: CustomMaskEditText, valid: Boolean, message: String): Boolean {
        val ti = view.parent.parent as TextInputLayout
        if (!valid) {
            ti.error = message
            view.requestFocus()
        } else {
            ti.error = null
        }
        ti.isErrorEnabled = !valid
        return valid
    }

    private fun setupCustomNumberPicker() {
        picker?.minValue = 1
        picker?.maxValue = 5
        picker?.value = 3
        picker?.wrapSelectorWheel = false

        picker?.formatter = object : NumberPicker.Formatter, CustomNumberPicker.Formatter {
            override fun format(value: Int): String {
                return getValorFormatado(value)
            }
        }

        picker?.setOnValueChangedListener(object : CustomNumberPicker.OnValueChangeListener {
            override fun onValueChange(picker: CustomNumberPicker, oldVal: Int, newVal: Int) {
                Toast.makeText(this@MainActivity, "anterior = ${getValorFormatado(oldVal)} -> atual = ${getValorFormatado(newVal)}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getValorFormatado(value: Int) = (value * 2.5).formatMoney(true)
}
