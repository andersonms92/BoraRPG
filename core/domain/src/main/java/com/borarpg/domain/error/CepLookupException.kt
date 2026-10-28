package com.borarpg.domain.error

class CepLookupException(val error: CepLookupError, cause: Throwable? = null) :
    Exception(error.name, cause)
