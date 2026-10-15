package io.confluent.intellijplugin.dev

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dev-only: with -Didea.auto.reload.plugins=true the IDE reloads a plugin whose jar changed,
 * but only once a VFS refresh sees the change — normally on the next IDE window activation.
 * This watches our own lib/ folder and triggers that refresh right away, so deploy-local.sh
 * takes effect without switching apps.
 */
@Service(Service.Level.APP)
internal class HotReloadWatcher(private val scope: CoroutineScope) {
    private val started = AtomicBoolean()

    fun start() {
        if (!java.lang.Boolean.getBoolean("idea.auto.reload.plugins")) return
        if (!started.compareAndSet(false, true)) return
        val libDir = PluginManagerCore.getPlugin(PluginId.getId("com.intellij.bigdatatools.kafka"))
            ?.pluginPath?.resolve("lib") ?: return
        thisLogger().info("Watching $libDir to hot-reload the plugin")

        scope.launch(Dispatchers.IO) {
            FileSystems.getDefault().newWatchService().use { watcher ->
                libDir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY)
                while (isActive) {
                    // poll, not take(): the loop must notice cancellation when the plugin unloads
                    val key = watcher.poll(1, TimeUnit.SECONDS) ?: continue
                    val jars = key.pollEvents().mapNotNull { it.context() as? Path }
                        .filter { it.toString().endsWith(".jar") }
                    key.reset()
                    if (jars.isEmpty()) continue
                    delay(1500) // let the copy finish
                    thisLogger().info("Plugin jar changed ($jars), refreshing VFS to trigger hot reload")
                    jars.mapNotNull { LocalFileSystem.getInstance().findFileByNioFile(libDir.resolve(it)) }
                        .forEach { VfsUtil.markDirtyAndRefresh(true, false, false, it) }
                }
            }
        }
    }
}

/** Starts the watcher on project open, and for open projects after a live reload. */
internal class HotReloadWatcherActivity : ProjectActivity {
    override suspend fun execute(project: Project) = service<HotReloadWatcher>().start()
}
