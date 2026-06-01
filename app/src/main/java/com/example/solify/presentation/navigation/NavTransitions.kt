package com.example.solify.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination

private const val NAV_ANIM_DURATION_MS = 300
private const val TAB_ANIM_DURATION_MS = 200

private val tabRoutes = setOf(
    Screen.Profile.route,
    Screen.Training.route,
    Screen.Lessons.route,
)

private fun NavDestination.tabRouteOrNull(): String? {
    var destination: NavDestination? = this
    while (destination != null) {
        if (destination.route in tabRoutes) return destination.route
        destination = destination.parent
    }
    return null
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.navEnterTransition(): EnterTransition =
    if (isTabToTabNavigation()) tabEnterTransition() else horizontalEnterTransition()

fun AnimatedContentTransitionScope<NavBackStackEntry>.navExitTransition(): ExitTransition =
    if (isTabToTabNavigation()) tabExitTransition() else horizontalExitTransition()

fun AnimatedContentTransitionScope<NavBackStackEntry>.navPopEnterTransition(): EnterTransition =
    if (isTabToTabNavigation()) tabEnterTransition() else horizontalPopEnterTransition()

fun AnimatedContentTransitionScope<NavBackStackEntry>.navPopExitTransition(): ExitTransition =
    if (isTabToTabNavigation()) tabExitTransition() else horizontalPopExitTransition()

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabToTabNavigation(): Boolean =
    initialState.destination.tabRouteOrNull() != null &&
        targetState.destination.tabRouteOrNull() != null

private fun AnimatedContentTransitionScope<*>.horizontalEnterTransition(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(NAV_ANIM_DURATION_MS)
    ) + fadeIn(tween(NAV_ANIM_DURATION_MS))

private fun AnimatedContentTransitionScope<*>.horizontalExitTransition(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec = tween(NAV_ANIM_DURATION_MS)
    ) + fadeOut(tween(NAV_ANIM_DURATION_MS))

private fun AnimatedContentTransitionScope<*>.horizontalPopEnterTransition(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec = tween(NAV_ANIM_DURATION_MS)
    ) + fadeIn(tween(NAV_ANIM_DURATION_MS))

private fun AnimatedContentTransitionScope<*>.horizontalPopExitTransition(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(NAV_ANIM_DURATION_MS)
    ) + fadeOut(tween(NAV_ANIM_DURATION_MS))

private fun tabEnterTransition(): EnterTransition = fadeIn(tween(TAB_ANIM_DURATION_MS))

private fun tabExitTransition(): ExitTransition = fadeOut(tween(TAB_ANIM_DURATION_MS))
