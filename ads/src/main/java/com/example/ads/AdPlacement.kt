package com.example.ads

@JvmInline
value class AdPlacement(val id: String) {
    companion object {
        val Default: AdPlacement = AdPlacement("default")
    }
}
