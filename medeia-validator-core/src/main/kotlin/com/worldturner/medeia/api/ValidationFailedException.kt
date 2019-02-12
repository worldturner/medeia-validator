package com.worldturner.medeia.api

class ValidationFailedException(val failures: List<FailedValidationResult>) :
    RuntimeException(failures.toString()) {

    constructor(vararg failures: FailedValidationResult) : this(failures.toList())
}
