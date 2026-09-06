package com.fati_market

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fati_market.ui.components.BrandMark
import com.fati_market.ui.theme.LocalMarketAccents
import com.fati_market.ui.theme.brandGradient
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, destination: String = "login") {
    val accents = LocalMarketAccents.current
    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "splash_alpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.86f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "splash_scale",
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(1600)
        navController.navigate(destination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brandGradient()),
    ) {
        // Two soft discs give the flat gradient a little depth without
        // needing an image asset.
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-120).dp, y = (-80).dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(260.dp)
                .offset(x = 90.dp, y = 110.dp)
                .background(accents.reward.copy(alpha = 0.10f), CircleShape),
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(alpha)
                .scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            BrandMark(size = 96.dp, icon = Icons.Filled.Storefront)

            Text(
                text = "Fati-Market",
                style = MaterialTheme.typography.displaySmall,
                color = accents.onBrand,
            )

            Text(
                text = "Ofelia's Store  ·  Our Lady of Fatima University",
                style = MaterialTheme.typography.bodySmall,
                color = accents.onBrandMuted,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp)
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                color = accents.onBrand,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Buy, sell and swap school supplies",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = accents.onBrandMuted,
            )
        }
    }
}
