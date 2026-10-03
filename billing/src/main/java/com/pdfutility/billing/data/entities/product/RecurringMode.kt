package com.pdfutility.billing.data.entities.product

/**
 * Defines the type of a subscription pricing phase.
 *
 * A subscription may transition through multiple pricing modes over time,
 * and this enum distinguishes between those modes:
 *
 * - [FREE]       → A 100% free trial period, typically limited in duration.
 * - [DISCOUNTED] → A temporarily reduced price (e.g., intro offer).
 * - [ORIGINAL]   → The full, standard price after any trials or discounts.
 */
enum class RecurringMode {
    FREE,
    DISCOUNTED,
    ORIGINAL
}