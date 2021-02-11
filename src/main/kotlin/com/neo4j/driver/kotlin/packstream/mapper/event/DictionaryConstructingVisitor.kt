package com.neo4j.driver.kotlin.packstream.mapper.event

import com.neo4j.driver.kotlin.packstream.event.DictionaryVisitor

interface DictionaryConstructingVisitor<V : Any> : ConstructingVisitor<V>, DictionaryVisitor