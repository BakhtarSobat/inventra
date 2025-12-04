package com.bsobat.inventra.data.di

interface ContextProvider {
    operator fun invoke(): Any
}