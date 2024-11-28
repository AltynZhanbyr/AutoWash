package com.example.buildsrc

import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

fun String.escaped() = "\"$this\""

/** @see <a href="https://stackoverflow.com/a/60474096/9846834">Source code</a>*/
fun Project.getProperty(key: String, file: String = "local.properties"): String {
    val properties = Properties()
    val localProperties = File(file)
    if (!localProperties.isFile) error("File ${file} not found")

    InputStreamReader(
        FileInputStream(localProperties),
        Charsets.UTF_8
    ).use(properties::load)

    return properties.getProperty(key)
}