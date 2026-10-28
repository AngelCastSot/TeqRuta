package com.example.sqlite.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sqlite.ui.TrasladoViewModel

@Composable
fun LoginScreen(vm: TrasladoViewModel) {
    val login by vm.login.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "TeqRuta",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text("TEQMED SpA - Traslados en terreno", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            BadgeOffline()
            Spacer(Modifier.height(24.dp))

            CampoTexto(
                campo = login.correo,
                etiqueta = "Correo corporativo",
                icono = Icons.Default.Email,
                alCambiar = vm::onCorreoChange,
                tipoTeclado = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            CampoTexto(
                campo = login.clave,
                etiqueta = "Contraseña",
                icono = Icons.Default.Lock,
                alCambiar = vm::onClaveChange,
                tipoTeclado = KeyboardType.Password,
                esClave = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { vm.iniciarSesion() }, modifier = Modifier.fillMaxWidth()) {
                Text("Ingresar")
            }
        }
    }
}
