# Change Log

### Release 1.0.0
First public release

### Release 1.1.0

Bug fixes:
* Perform schema validation before schema mapping to object model
* When calling `nextString` on Gson reader decorator when next token is
  a number, incorrect token type `TEXT` was sent to validator. 

Improvements:

* Report line numbers and file name for schema loading errors
* Expanded README.md
* Added `copyStream` method to API to allow copying stream while
  validating without Jackson or Gson API calls.
* Added `TokenLocationException` to the public API; this exception can
  be caught and queried for the location of the error.
* When a `baseUri` is specified in the `SchemaSource`, the schema is
  always registered under this URI too, regardless of any `$id` on 
  the root of the schema. This is useful for providing a known URI
  to reference a schema without looking inside the schema. 