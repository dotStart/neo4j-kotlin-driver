package com.neo4j.driver.kotlin.packstream.event

interface DictionaryVisitor : Visitor {

    fun visitKey(key: String) = Unit
}