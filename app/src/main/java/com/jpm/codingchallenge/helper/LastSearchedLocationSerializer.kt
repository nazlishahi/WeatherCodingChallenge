package com.jpm.codingchallenge.helper

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.jpm.codingchallenge.LastSearchedLocation
import java.io.InputStream
import java.io.OutputStream

object LastSearchedLocationSerializer: Serializer<LastSearchedLocation> {

    override val defaultValue: LastSearchedLocation
        get() = LastSearchedLocation.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): LastSearchedLocation {
        try {
            return LastSearchedLocation.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: LastSearchedLocation, output: OutputStream) {
        t.writeTo(output)
    }
}