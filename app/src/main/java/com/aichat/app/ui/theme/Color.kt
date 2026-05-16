package com.aichat.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand ──────────────────────────────────────────────
val Brand        = Color(0xFF6366F1)   // Indigo-500
val BrandLight   = Color(0xFF818CF8)   // Indigo-400
val BrandDark    = Color(0xFF4F46E5)   // Indigo-600
val BrandSurface = Color(0xFFEEF2FF)   // Indigo-50

// ── Light Palette ──────────────────────────────────────
val LightBackground   = Color(0xFFF8FAFC)
val LightSurface      = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnBackground  = Color(0xFF0F172A)
val LightOnSurface     = Color(0xFF1E293B)
val LightOnSurfaceVar  = Color(0xFF64748B)
val LightOutline       = Color(0xFFE2E8F0)
val LightError         = Color(0xFFEF4444)
val LightSuccess       = Color(0xFF22C55E)

// ── Dark Palette ───────────────────────────────────────
val DarkBackground    = Color(0xFF0B1120)
val DarkSurface       = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)
val DarkOnBackground   = Color(0xFFF1F5F9)
val DarkOnSurface      = Color(0xFFE2E8F0)
val DarkOnSurfaceVar   = Color(0xFF94A3B8)
val DarkOutline        = Color(0xFF334155)
val DarkError          = Color(0xFFF87171)
val DarkSuccess        = Color(0xFF4ADE80)

// ── Chat Bubbles ───────────────────────────────────────
val UserBubbleLight = Brand
val UserBubbleDark  = BrandLight
val AIBubbleLight   = Color(0xFFF1F5F9)
val AIBubbleDark    = Color(0xFF1E293B)

// Old aliases kept for minimal diff (will be removed later)
val Primary  = Brand
val Success  = Color(0xFF22C55E)
val Surface  = Color(0xFFFFFFFF)  // placeholder — actual value from theme
val AIBubble = Color(0xFFF1F5F9)  // placeholder
val UserBubble = Brand
val Background = Color(0xFFF8FAFC)  // placeholder
