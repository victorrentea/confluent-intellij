package io.confluent.intellijplugin.toolwindow.controllers

import com.intellij.testFramework.LightVirtualFile
import io.confluent.intellijplugin.icons.BigdatatoolsKafkaIcons
import com.intellij.openapi.fileTypes.FileType

/** Fake file type to supply Consumer and Producer editor tabs with icon. */
class KafkaFileType : FileType {
    override fun getName() = ""
    override fun getDescription() = ""
    override fun getDefaultExtension() = ""
    override fun getIcon() = BigdatatoolsKafkaIcons.ConfluentTab
    override fun isBinary() = true
}

/**
 * Consumer/producer editor file. A plugin class on purpose: on unload the IDE forgets closed tabs whose file
 * class comes from the plugin; a plain LightVirtualFile with our KafkaFileType kept the plugin classloader alive
 * through the "reopen closed tab" history.
 */
class KafkaEditorFile(name: String) : LightVirtualFile(name, KafkaFileType(), "")
