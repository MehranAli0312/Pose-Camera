package com.aipose.camera.posematch.ui.firebaseRemote

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

open class RemoteValueEnumSerializer<E : Enum<E>>(
    serialName: String,
    private val entries: List<E>,
    private val fallback: E,
    private val remoteValueOf: (E) -> Long,
) : KSerializer<E> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName, PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: E) {
        encoder.encodeLong(remoteValueOf(value))
    }

    override fun deserialize(decoder: Decoder): E {
        val remoteValue = decoder.decodeLong()
        return entries.firstOrNull { remoteValueOf(it) == remoteValue } ?: fallback
    }
}
