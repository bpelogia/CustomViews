package br.com.bpelogia.appteste.flow.dashboard

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import br.com.bpelogia.appteste.R
import br.com.bpelogia.viewcustom.extensions.moveDownViewOnScrolling
import br.com.bpelogia.viewcustom.ui.CustomMaskEditText
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
        imprimir("Placa", et_plate)
        imprimir("Valor", et_monetary)
        imprimir("Cep", et_cep)
        imprimir("Data", et_date)
        imprimir("Telefone", et_phone)
        imprimir("Normal", et_normal)
    }

    fun imprimir(fieldName: String, field: CustomMaskEditText) {
        val message = if(field.isValid) "$fieldName Válido! - ${field.text}" else "$fieldName INVÁLIDO!! - ${field.text}"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scroll_view.moveDownViewOnScrolling(navigation)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bt_validate.setOnClickListener { validarCampos() }
    }
}
