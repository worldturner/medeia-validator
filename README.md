Medeia Validator
================

Medeia validator is a streaming validator for json data using schema documents specified in 
the Json Schema format.

Json Schema version support
---------------------------

Medeia supports the following versions of the [Json Schema specification](https://json-schema.org/):

* Draft-04
* Draft-06
* Draft-07

Medeia can validate Json data and can convert schema documents from draft-04 to draft-07.

Parser library support
----------------------

Medeia works with the following Json parser libraries:

* FasterXML Jackson (only jackson-core is needed)
* Google Gson

Use cases
---------

Medeia validator was written with the following use-cases in mind:

1. Validate Json data as it is being read into a tree or into an object model using an object mapper
2. Validate Json data as it is being serialized from a tree or object model
3. Validate Json data in a message router or validator component on the network, which has no need to
  load the Json data into a tree or object model
  
Streaming validation
--------------------

Medeia does not build a full internal tree of the Json data while it is validating; it only temporarily stores 
as much as is needed for processing the current validation rules.

This ensures a lower memory footprint and tha ability to parse very large documents, even documents that do
not fit into memory. This is beneficial for the use cases for which Medeia was written.

Although memory versus speed (or CPU utilization) is often a trade-off, Medeia validator is very fast for
its use-cases. Validation approaches that requires an in-memory model of the data first spent time building that model,
and also spend extra time garbage collecting that model afterwards.

### Caveats

* Properties in objects do not have to be in any particular order; two objects are equal in Json even if the
  properties are in different orders. As a result, many validation rules require the validator to wait until 
  all properties have been seen; in the meantime, property names have to be stored in memory.
* The uniqueItems validator would seem to require in-memory trees. Medeia-validator does not in fact do that
  by default; instead, Medeia builds cryptographic hashes for the list objects it sees so that
  it does not have to store the entire contents of the object in memory. This is a CPU versus memory trade-off.
* The dependencies and the if/then/else validators have validators that are only evaluated if another
  validator matches. Medeia always evaluates the dependent validators (and the then/else) clauses, even when their
  results need to be discarded because of the result of the condition. The alternative would require an in-memory 
  tree. This is a CPU versus memory trade-off.


Kotlin, Java and JVM languages support
--------------------------------------

Supports calling from Kotlin and Java, and other languages that support the JVM that can call Java APIs.

All accessible types are in the `com.worldturner.medeia.api.*` packages; classes in other packages are not guaranteed
to remain stable across versions, they can change at any time witout notice.

Versioning
----------

The versioning scheme of this library is [Semantic Versioning](https://semver.org/) but only for types in the API.
These types have a package name that starts with `com.worldturner.medeia.api`.
The APIs of types in other packages can change at any time even between minor versions.

Source Json format support
--------------------------
* Ordinary JSON
* [Multi-line JSON](http://jsonlines.org/) for Jackson (not tested for Gson)

How to use
----------

#####Kotlin:

```kotlin
val medeia = MedeiaJacksonApi()
val schemaSource = PathSchemaSource(Paths.get("schemas/myschema.json"))
val validator = medeia.loadSchema(schemaSource)
// Creates a validating parser
val parser = medeia.decorateJsonParser(validator, JsonFactory().createParser(mydata))
val deserialized = ObjectMapper().readValue(parser, MyClass::class.java)
```

To just perform validation rather than deserialising objects, one can do:
```kotlin
while (parser.nextToken() != null) {
}
```

The `MedeiaJacksonApi` and `MedeiaGsonApi` classes have various methods to load schemas and to create validating
parsers/generators (or readers/writers in Gson parlance)

The interface `SchemaSource` has several implementations to load schemas from InputStreams, Readers, Paths, or memory.

The version of a schema is automatically detected, but if the schema file doesn't specify it using a `$schema` field,
the version can be provided through the SchemaSource.

Mixing different versions of schemas (draft4, 6 and 7) is allowed and schemas can refer to remote schemas in 
different versions than their own.

Options are passed using a `JsonSchemaValidationOptions` object.


#####Java:

Care has been taken that all methods in the API can be invoked from Java. The `JsonSchemaValidationOptions` has 
`with*` methods to allow option setting from Java.

```java
TBD
```
Test Suite Support
------------------

Medeia validator passes all 424 'required' tests of the JSON-Schema-Test-Suite testsuite.
It passes 132 out of the 143 optional tests. The 11 failing optional tests concern "format" keyword
validation where the following formats that are not yet (fully) supported:

> uri, uri-template, iri, email, relative-json-pointer, idn-email, regex

Format keyword validation is optional (and can be turned off as mandated by the specification)

The following formats are supported and pass the 'optional' testsuite:

> json-pointer, date, time, date-time, ipv4, ipv4, hostname, idn-hostname

