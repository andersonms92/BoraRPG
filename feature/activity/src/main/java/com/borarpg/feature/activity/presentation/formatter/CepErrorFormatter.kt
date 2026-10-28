package com.borarpg.feature.activity.presentation.formatter

import com.borarpg.domain.error.CepLookupError

internal val CepLookupError.message: String
    get() =
        when (this) {
            CepLookupError.INVALID_CEP -> "Digite um CEP válido, como 01310-100."
            CepLookupError.NOT_FOUND ->
                "CEP não encontrado. Confira os números e tente novamente."
            CepLookupError.LOCATION_UNAVAILABLE ->
                "O CEP existe, mas não possui posição no mapa. Tente um CEP próximo ou mova o mapa manualmente."
            CepLookupError.SERVICE_UNAVAILABLE ->
                "A busca por CEP está indisponível. Tente novamente em instantes."
            CepLookupError.CONNECTION ->
                "Não foi possível consultar o CEP. Verifique sua conexão e tente novamente."
            CepLookupError.INVALID_RESPONSE ->
                "A busca por CEP retornou uma resposta inválida."
        }
