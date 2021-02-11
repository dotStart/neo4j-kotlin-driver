package com.neo4j.driver.kotlin

import kotlinx.coroutines.runBlocking
import java.net.URI

fun main(args: Array<String>) {
    runBlocking {
        // Let's program a happy little neo4j driver :)
        Driver(URI.create("bolt://localhost:11003"), "neo4j", "1234").use { driver ->
            driver.connect().use { con ->
                val result = con.run("UNWIND range(1, 3) AS n RETURN n", emptyMap())
                println(result)

                con.run("SOME RUBBISH", emptyMap())
            }
        }
    }
}