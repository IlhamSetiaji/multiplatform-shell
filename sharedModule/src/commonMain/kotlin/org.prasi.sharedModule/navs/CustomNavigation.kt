package org.prasi.sharedModule.navs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun CustomNavigationSample() {
    CustomNavigationScreen()
}

@Composable
fun <T : Any> Navigation(
    currentScreen: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    Box(modifier) {
        saveableStateHolder.SaveableStateProvider(currentScreen) {
            content(currentScreen)
        }
    }
}

@Composable
fun CustomNavigationScreen() {
    var screen by rememberSaveable { mutableStateOf("screen1") }
    Scaffold(
        content = {
            Navigation(screen, Modifier.fillMaxSize()) { currentScreen ->
                if (currentScreen == "screen1") {
                    Home()
                } else {
                    Personal()
                }
            }
        },
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    selected = screen == "screen1",
                    onClick = { screen = "screen1" },
                    icon = {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Home),
                            contentDescription = "Home",
                        )
                    },
                )
                BottomNavigationItem(
                    selected = screen == "screen2",
                    onClick = { screen = "screen2" },
                    icon = {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.Person),
                            contentDescription = "Personal",
                        )
                    },
                )
            }
        },
    )
}
