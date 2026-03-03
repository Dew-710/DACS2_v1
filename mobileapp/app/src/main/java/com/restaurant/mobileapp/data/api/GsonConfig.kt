package com.restaurant.mobileapp.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.math.BigDecimal

object GsonConfig {
    
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(BigDecimal::class.java, BigDecimalTypeAdapter())
        .setLenient()
        .create()
    
    val converterFactory: GsonConverterFactory = GsonConverterFactory.create(gson)
}

class BigDecimalTypeAdapter : JsonSerializer<BigDecimal>, JsonDeserializer<BigDecimal> {
    override fun serialize(
        src: BigDecimal?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src?.toString())
    }
    
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): BigDecimal? {
        return when {
            json == null -> null
            json.isJsonNull -> null
            json.isJsonPrimitive -> {
                val primitive = json.asJsonPrimitive
                when {
                    primitive.isString -> {
                        val stringValue = primitive.asString
                        if (stringValue.isEmpty()) null else BigDecimal(stringValue)
                    }
                    primitive.isNumber -> BigDecimal(primitive.asString)
                    else -> null
                }
            }
            else -> null
        }
    }
}

