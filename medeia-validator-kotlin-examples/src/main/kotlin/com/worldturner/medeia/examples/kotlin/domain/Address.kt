package com.worldturner.medeia.examples.kotlin.domain

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Address(
    val street1: String,
    val street2: String?,
    val city: String,
    val postalCode: String,
    val region: String?,
    val country: String?
)
