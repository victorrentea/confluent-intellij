package io.confluent.intellijplugin.dev

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.WatchService
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dev-only: with -Didea.auto.reload.plugins=true the IDE reloads a plugin whose jar changed,
 * but only once a VFS refresh sees the change — normally on the next IDE window activation.
 * This watches our own lib/ folder and triggers that refresh right away, so deploy-local.sh
 * takes effect without switching apps.
 *
 * A plain daemon thread, not the service coroutine scope (against this repo's convention, on purpose):
 * a coroutine left on the shared IO pool stayed referenced by a parked worker past the IDE's 5s unload
 * check and pinned the plugin classloader. One-shot: it exits after triggering the reload, and the
 * freshly loaded plugin starts its own watcher.
 */
@Service(Service.Level.APP)
internal class HotReloadWatcher : Disposable {
    private val started = AtomicBoolean()
    @Volatile private var watchService: WatchService? = null
    @Volatile private var thread: Thread? = null

    fun start() {
        if (!java.lang.Boolean.getBoolean("idea.auto.reload.plugins")) return
        if (!started.compareAndSet(false, true)) return
        val libDir = PluginManagerCore.getPlugin(PluginId.getId("com.intellij.bigdatatools.kafka"))
            ?.pluginPath?.resolve("lib") ?: return
        val ws = FileSystems.getDefault().newWatchService().also { watchService = it }
        libDir.register(ws, ENTRY_CREATE, ENTRY_MODIFY)
        thisLogger().info("Watching $libDir to hot-reload the plugin")
        thread = Thread({ watch(ws, libDir) }, "Kafka plugin hot-reload watcher").apply {
            isDaemon = true
            start()
        }
    }

    private fun watch(ws: WatchService, libDir: Path) {
        try {
            while (true) {
                val key = ws.take()
                val jars = key.pollEvents().mapNotNull { it.context() as? Path }.filter { it.toString().endsWith(".jar") }
                key.reset()
                if (jars.isEmpty()) continue
                Thread.sleep(1500) // let the copy finish
                thisLogger().info("Plugin jar changed ($jars), refreshing VFS to trigger hot reload")
                jars.mapNotNull { LocalFileSystem.getInstance().findFileByNioFile(libDir.resolve(it)) }
                    .forEach { VfsUtil.markDirtyAndRefresh(true, false, false, it) }
                return
            }
        } catch (_: InterruptedException) {
        } catch (_: ClosedWatchServiceException) {
        } finally {
            runCatching { ws.close() }
        }
    }

    override fun dispose() {
        runCatching { watchService?.close() }
        thread?.interrupt()
        thread = null
        watchService = null
    }
}

/** Starts the watcher on project open, and for open projects after a live reload. */
internal class HotReloadWatcherActivity : ProjectActivity {
    override suspend fun execute(project: Project) = service<HotReloadWatcher>().start()
}
