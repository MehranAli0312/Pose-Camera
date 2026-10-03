package com.pdfutility.billing.utilities.extensions

/* ——— List extension to overwrite contents ——— */
internal fun <T> MutableList<T>.setAll(newItems: List<T>) {
    clear(); addAll(newItems)
}