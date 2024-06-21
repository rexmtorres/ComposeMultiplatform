package com.rexmtorres.cmp

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.ScreenTransition
import cafe.adriel.voyager.transitions.ScreenTransitionContent
import cafe.adriel.voyager.transitions.SlideOrientation
import cafe.adriel.voyager.transitions.SlideTransition
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.home
import kotlinproject.composeapp.generated.resources.messages
import kotlinproject.composeapp.generated.resources.profile
import kotlinproject.composeapp.generated.resources.settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 *
 * @property label String
 * @property unselectedIcon ImageVector
 * @property selectedIcon ImageVector
 * @property badge Int? A zero or positive value indicates a badge count to be displayed.  A
 * negative value indicates a dot to be displayed.  `null` indicates no count/dot will be displayed.
 * @constructor
 */
data class BottomNavigationItem(
    val screen: Screen,
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val badge: Int? = null
)

class MainScreen : Screen {
    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()

        val navigationItems = listOf(
            BottomNavigationItem(
                screen = HomeScreen(),
                label = stringResource(Res.string.home),
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home
            ),
            BottomNavigationItem(
                screen = MessagesScreen(),
                label = stringResource(Res.string.messages),
                selectedIcon = Icons.Filled.Email,
                unselectedIcon = Icons.Outlined.Email
            ),
            BottomNavigationItem(
                screen = ProfileScreen(),
                label = stringResource(Res.string.profile),
                selectedIcon = Icons.Filled.AccountBox,
                unselectedIcon = Icons.Outlined.AccountBox
            ),
            BottomNavigationItem(
                screen = SettingsScreen(),
                label = stringResource(Res.string.settings),
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings
            ),
        )

        val initialScreen = HomeScreen()

        var currentScreen by remember {
            mutableStateOf<Screen>(initialScreen)
        }

        var newScreen by remember {
            mutableStateOf<Screen>(initialScreen)
        }

        Navigator(
            screen = initialScreen,
            disposeBehavior = NavigatorDisposeBehavior(disposeSteps = false),
            onBackPressed = {
                true
            }
        ) { navigator ->
            Scaffold(
                bottomBar = {
                    BottomNavigation {
                        navigationItems.forEach { item ->
                            val currentKey = navigator.lastItem.key
                            val itemKey = item.screen.key
                            val selected = currentKey == itemKey

                            BottomNavigationItem(
                                selected = selected,
                                onClick = {
                                    println("== ==> onClick")
                                    newScreen = item.screen

                                    coroutineScope.launch {
                                        delay(200)

                                        val exists = navigator.popUntil { screen ->
                                            item.screen.key == screen.key
                                        }

                                        if (!exists) {
                                            navigator.push(item.screen)
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) {
                                            item.selectedIcon
                                        } else {
                                            item.unselectedIcon
                                        },
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label
                                    )
                                }
                            )
                        }
                    }
                }
            ) {
                val prevScreenIndex = remember(newScreen) { getScreenIndex(currentScreen) }

                val newScreenIndex = remember(newScreen) { getScreenIndex(newScreen) }

                if (prevScreenIndex < newScreenIndex) {
                    println("-----------------------------------------")
                    println("right to left")
                    println("currentScreen = $prevScreenIndex -> $currentScreen")
                    println("newScreen = $newScreenIndex -> $newScreen")

                    SlideTransition(
                        navigator = navigator,
                        disposeScreenAfterTransitionEnd = true
                    ) { screen ->
                        screen.Content()
                        currentScreen = screen
                    }
                } else if (prevScreenIndex > newScreenIndex) {
                    println("-----------------------------------------")
                    println("left to right")
                    println("currentScreen = $prevScreenIndex -> $currentScreen")
                    println("newScreen = $newScreenIndex -> $newScreen")

                    SlideLeftToRightTransition(
                        navigator = navigator,
                        //disposeScreenAfterTransitionEnd = true
                    ) { screen ->
                        screen.Content()
                        currentScreen = screen
                    }
                } else {
                    println("-----------------------------------------")
                    println("stay")
                    println("currentScreen = $prevScreenIndex -> $currentScreen")
                    println("newScreen = $newScreenIndex -> $newScreen")

                    CurrentScreen()
                }
            }
        }
    }
}

private fun getScreenIndex(screen: Screen): Int = when (screen) {
    is HomeScreen -> 0
    is MessagesScreen -> 1
    is ProfileScreen -> 2
    is SettingsScreen -> 3
    else -> -1
}

@ExperimentalVoyagerApi
@Composable
fun SlideLeftToRightTransition(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    orientation: SlideOrientation = SlideOrientation.Horizontal,
    animationSpec: FiniteAnimationSpec<IntOffset> = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    ),
    disposeScreenAfterTransitionEnd: Boolean = false,
    content: ScreenTransitionContent = { it.Content() }
) {
    ScreenTransition(
        navigator = navigator,
        modifier = modifier,
        disposeScreenAfterTransitionEnd = disposeScreenAfterTransitionEnd,
        content = content,
        transition = {
            val (initialOffset, targetOffset) = when (navigator.lastEvent) {
                StackEvent.Pop -> ({ size: Int -> size }) to ({ size: Int -> -size })
                else -> ({ size: Int -> -size }) to ({ size: Int -> size })
            }

            when (orientation) {
                SlideOrientation.Horizontal ->
                    slideInHorizontally(animationSpec, initialOffset) togetherWith
                            slideOutHorizontally(animationSpec, targetOffset)
                SlideOrientation.Vertical ->
                    slideInVertically(animationSpec, initialOffset) togetherWith
                            slideOutVertically(animationSpec, targetOffset)
            }
        }
    )
}
