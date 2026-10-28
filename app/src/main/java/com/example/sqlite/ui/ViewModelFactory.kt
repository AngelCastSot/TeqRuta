package com.example.sqlite.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sqlite.data.AppDatabase
import com.example.sqlite.data.TrasladoRepository

// Inyección manual de dependencias (sin Hilt): aquí se arma DAO -> Repository -> ViewModel
class TrasladoViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    private val repo by lazy {
        val dao = AppDatabase.get(app).trasladoDao()
        TrasladoRepository(dao)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        TrasladoViewModel(app, repo) as T
}
