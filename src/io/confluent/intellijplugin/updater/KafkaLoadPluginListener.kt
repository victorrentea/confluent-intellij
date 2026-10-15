package io.confluent.intellijplugin.updater

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import io.confluent.intellijplugin.core.constants.BdtPlugins
import io.confluent.intellijplugin.telemetry.logUser

internal class KafkaLoadPluginListener : DynamicPluginListener {
    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        if (pluginDescriptor.pluginId.idString != BdtPlugins.KAFKA_ID)
            return

        // No "plugin installed — Try It" balloon: it popped on every dev hot reload
        // Send initial identify when plugin is installed dynamically
        logUser(buildMap {
            put("pluginVersion", pluginDescriptor.version)
            put("pluginInstalled", true)
        })
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId.idString != BdtPlugins.KAFKA_ID)
            return

        // Send final identify event before plugin is uninstalled
        logUser(buildMap {
            put("pluginVersion", pluginDescriptor.version)
            put("pluginInstalled", false)
            put("isUpdate", isUpdate)
        })
    }
}