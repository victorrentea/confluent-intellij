package io.confluent.intellijplugin.client

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import io.confluent.intellijplugin.core.util.executeOnPooledThread
import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.AdminClient
import java.time.Duration

class BdtKafkaAdminClient(private val adminClient: AdminClient) : Admin by adminClient, Disposable {
    override fun dispose() = executeOnPooledThread {
        try {
            // Duration.ZERO: the admin thread (whose context classloader is the plugin's) must be gone
            // before the IDE's 5s unload check; a graceful 10s close kept the plugin from unloading
            adminClient.close(Duration.ZERO)
        } catch (t: Throwable) {
            logger.warn("Cannot close kafka client", t)
        }
    }

    companion object {
        private val logger = Logger.getInstance(this::class.java)
    }
}