package com.worldturner.medeia.examples.kotlin.domain

fun createValidPersonFixture(): Person =
    Person(
        firstName = "Yon",
        lastName = "Yonson",
        address = Address(
            region = "Wisconsin",
            street1 = "1 Lumberjack Road",
            street2 = null,
            city = "Townsville",
            postalCode = "34216",
            country = "USA"
        )
    )

fun createInvalidPersonFixture(): Person {
    val person = createValidPersonFixture()
    return person.copy(address = person.address.copy(city = ""))
}