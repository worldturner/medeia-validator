package com.worldturner.medeia.parser

import com.worldturner.medeia.parser.type.ArrayType
import com.worldturner.medeia.parser.type.BooleanOrObjectType
import com.worldturner.medeia.parser.type.BooleanType
import com.worldturner.medeia.parser.type.NumberType
import com.worldturner.medeia.parser.type.ObjectType
import com.worldturner.medeia.parser.type.PropertyType
import com.worldturner.medeia.parser.type.SingleOrArrayType
import com.worldturner.medeia.parser.type.TextType
import com.worldturner.medeia.testing.support.JsonParserLibrary
import com.worldturner.medeia.testing.support.parse
import com.worldturner.medeia.types.SingleOrList
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

data class Person(
    val name: String,
    val age: Int,
    val address: SingleOrList<Address>
)

data class Address(
    val addressLines: List<String>,
    val city: String,
    val country: String? = null,
    val validated: Boolean
)

data class Validation(
    val booleanValue: Boolean? = null,
    val description: String? = null,
    val minimum: Int? = null
)

val validationBoolean = """
    false
"""

val validationObject = """
    {
        "description": "This is aType description",
        "minimum": 3
    }
"""

val validationObjectUnknownPropertySingle = """
    {
        "description": "This is aType description",
        "minimum": 3,
        "unknown": 25.0
    }
"""

val validationObjectUnknownPropertyStructured = """
    {
        "description": "This is aType description",
        "minimum": 3,
        "unknown": [25.0, {"abc": "def"}]
    }
"""

val singleAddress = """
{
    "addressLines": [
        "c/o Ann Doe",
        "42 Street"
    ],
    "city": "Bloomingham",
    "country": "Brexitonia",
    "%validated": true
}
"""

val singleAddress2 = """
{
    "addressLines": [
        "16 Lane #16-03"
    ],
    "city": "McMurdo Station",
    "country": "Antarctica",
    "%validated": false
}
"""

val singleAddressNoCountry = """
{
    "addressLines": [
        "c/o Ann Doe",
        "42 Street"
    ],
    "city": "Bloomingham",
    "%validated": false
}
"""

val singlePerson = """
{
    "name": "Johnny Doe",
    "age": 32,
    "address": $singleAddress
}
"""

val singlePersonWith2Addresses = """
{
    "name": "Johnies Doe",
    "age": 3223,
    "address": [$singleAddress, $singleAddress2]
}
"""

val listOfPerson = """
[
    {
        "name": "Johnny Doe",
        "age": 32,
        "address": $singleAddress
    },
    {
        "name": "Yon Yonson",
        "age": 69,
        "address": $singleAddress
    }
]
"""

object AddressType : ObjectType(
    kotlinClass = Address::class,
    propertyTypes = listOf(
        PropertyType("addressLines", ArrayType(itemType = TextType)),
        PropertyType("city", TextType),
        PropertyType("country", TextType),
        PropertyType("%validated", BooleanType, "validated")
    )
)

object PersonType : ObjectType(
    kotlinClass = Person::class,
    propertyTypes = listOf(
        PropertyType("name", TextType),
        PropertyType("age", NumberType),
        PropertyType("address", SingleOrArrayType(AddressType))
    )
)

val validationObjectPropertyTypes = listOf(
    PropertyType("description", TextType),
    PropertyType("minimum", NumberType)
)

object ValidationObjectType : ObjectType(
    kotlinClass = Validation::class,
    propertyTypes = validationObjectPropertyTypes
)

object ValidationObjectIgnoreUnknownPropertiesType : ObjectType(
    kotlinClass = ValidationObjectType.kotlinClass,
    propertyTypes = validationObjectPropertyTypes,
    ignoreAdditionalProperties = true
)

val ValidationType = BooleanOrObjectType(ValidationObjectType, "booleanValue")

class SimpleObjectMapperTest {

    @Test
    fun `Parse single address`() {
        val address = parse(AddressType, singleAddress, JsonParserLibrary.GSON) as Address
        assertEquals(listOf("c/o Ann Doe", "42 Street"), address.addressLines)
        assertEquals("Bloomingham", address.city)
        assertEquals("Brexitonia", address.country)
        assertEquals(true, address.validated)
    }

    @Test
    fun `Parse single address without country`() {
        val address = parse(AddressType, singleAddressNoCountry, JsonParserLibrary.JACKSON) as Address
        assertEquals(listOf("c/o Ann Doe", "42 Street"), address.addressLines)
        assertEquals("Bloomingham", address.city)
        assertEquals(null, address.country)
        assertEquals(false, address.validated)
    }

    @Test
    fun `Parse single person`() {
        val person = parse(PersonType, singlePerson, JsonParserLibrary.GSON) as Person
        assertEquals("Johnny Doe", person.name)
        assertEquals(32, person.age)
        val address = person.address.single!!
        assertEquals(listOf("c/o Ann Doe", "42 Street"), address.addressLines)
        assertEquals("Bloomingham", address.city)
        assertEquals("Brexitonia", address.country)
        assertEquals(true, address.validated)
    }

    @Test
    fun `Parse single person with 2 addresses`() {
        val person = parse(PersonType, singlePersonWith2Addresses, JsonParserLibrary.JACKSON) as Person
        assertEquals("Johnies Doe", person.name)
        assertEquals(3223, person.age)
        val addresses = person.address.list!!
        assertEquals(
            Address(
                addressLines = listOf("c/o Ann Doe", "42 Street"),
                city = "Bloomingham",
                country = "Brexitonia",
                validated = true
            ),
            addresses[0]
        )
        assertEquals(
            Address(
                addressLines = listOf("16 Lane #16-03"),
                city = "McMurdo Station",
                country = "Antarctica",
                validated = false
            ),
            addresses[1]
        )
    }

    @Test
    fun `Parse person list`() {
        @Suppress("UNCHECKED_CAST")
        val list = parse(ArrayType(PersonType), listOfPerson, JsonParserLibrary.JACKSON) as List<Person>
        val address = Address(
            addressLines = listOf("c/o Ann Doe", "42 Street"),
            city = "Bloomingham",
            country = "Brexitonia",
            validated = true
        )
        assertEquals(
            listOf(
                Person(
                    name = "Johnny Doe",
                    age = 32,
                    address = SingleOrList(single = address)
                ),
                Person(
                    name = "Yon Yonson",
                    age = 69,
                    address = SingleOrList(single = address)
                )
            ), list
        )
    }

    @Test
    fun `Parse BooleanOrObject with boolean`() {
        val validation = parse(ValidationType, validationBoolean, JsonParserLibrary.GSON) as Validation
        assertEquals(false, validation.booleanValue)
        assertNull(validation.description)
        assertNull(validation.minimum)
    }

    @Test
    fun `Parse BooleanOrObject with object`() {
        val validation = parse(ValidationType, validationObject, JsonParserLibrary.JACKSON) as Validation
        assertNull(validation.booleanValue)
        assertEquals("This is aType description", validation.description)
        assertEquals(3, validation.minimum)
    }

    @Test(expected = Exception::class)
    fun `Parse with unknown property should fail`() {
        parse(ValidationObjectType, validationObjectUnknownPropertySingle, JsonParserLibrary.GSON) as Validation
    }

    @Test
    fun `Parse with single unknown property should succeed if 'ignoreUnknownProperties' is true`() {
        assertNotNull(
            parse(
                ValidationObjectIgnoreUnknownPropertiesType,
                validationObjectUnknownPropertySingle,
                JsonParserLibrary.JACKSON
            )
        )
    }

    @Test
    fun `Parse with structured unknown property should succeed if 'ignoreUnknownProperties' is true`() {
        assertNotNull(
            parse(
                ValidationObjectIgnoreUnknownPropertiesType,
                validationObjectUnknownPropertyStructured,
                JsonParserLibrary.GSON
            )
        )
    }
}
