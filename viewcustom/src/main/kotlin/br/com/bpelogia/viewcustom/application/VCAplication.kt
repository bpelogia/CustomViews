package br.com.bpelogia.viewcustom.application

import android.app.Application

/**
 * @author Bruno Pelogia
 * @since 10/01/2018
 */
class VCAplication : Application() {

    companion object {
        lateinit var instance: VCAplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}