package com.sol.app

import android.app.Application

/** Classe Application : donne un acces global au contexte (pour la session). */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
