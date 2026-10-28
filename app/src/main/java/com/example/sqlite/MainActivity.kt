package com.example.sqlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.sqlite.ui.Pantalla
import com.example.sqlite.ui.TrasladoViewModel
import com.example.sqlite.ui.TrasladoViewModelFactory
import com.example.sqlite.ui.screen.LoginScreen
import com.example.sqlite.ui.screen.TrasladoFormScreen
import com.example.sqlite.ui.screen.TrasladoListScreen
import com.example.sqlite.ui.theme.SqliteTheme

class MainActivity : ComponentActivity() {
    private val vm: TrasladoViewModel by viewModels { TrasladoViewModelFactory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SqliteTheme { TeqRutaApp(vm) }
        }
    }
}

// Navegación sencilla: el ViewModel guarda la pantalla actual y aquí se dibuja la que corresponde
@Composable
fun TeqRutaApp(vm: TrasladoViewModel) {
    val pantalla by vm.pantalla.collectAsState()

    // El botón "atrás" del celular vuelve al listado desde el formulario
    BackHandler(enabled = pantalla == Pantalla.FORMULARIO) { vm.volverAlListado() }

    when (pantalla) {
        Pantalla.LOGIN -> LoginScreen(vm)
        Pantalla.LISTADO -> TrasladoListScreen(vm)
        Pantalla.FORMULARIO -> TrasladoFormScreen(vm)
    }
}
