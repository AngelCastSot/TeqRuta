package com.example.sqlite.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.sqlite.ui.Campo
import com.example.sqlite.ui.theme.VerdeOk

// Campo de texto reutilizable. NO valida nada: solo muestra lo que el ViewModel calculó en "campo".
@Composable
fun CampoTexto(
    campo: Campo,
    etiqueta: String,
    icono: ImageVector,
    alCambiar: (String) -> Unit,
    modifier: Modifier = Modifier,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    esClave: Boolean = false
) {
    OutlinedTextField(
        value = campo.valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        singleLine = true,
        leadingIcon = { Icon(icono, contentDescription = null) },
        // Ícono de la derecha: Error si hay error, CheckCircle si es válido, nada en otro caso
        trailingIcon = {
            if (campo.error != null) {
                Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
            } else if (campo.valido) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Válido", tint = VerdeOk)
            }
        },
        isError = campo.error != null,
        // El mensaje de error aparece y desaparece con animación
        supportingText = {
            AnimatedVisibility(visible = campo.error != null) {
                Text(campo.error ?: "", color = MaterialTheme.colorScheme.error)
            }
        },
        visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        modifier = modifier
    )
}

// Insignia que se muestra en la barra superior: indica que la app trabaja sin internet
@Composable
fun BadgeOffline() {
    Surface(color = VerdeOk, shape = RoundedCornerShape(50)) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CloudOff,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Modo Terreno Offline - Room Activo",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
