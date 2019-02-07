package com.worldturner.medeia.types

data class SingleOrList<T>(val single: T? = null, val list: List<T>? = null)
