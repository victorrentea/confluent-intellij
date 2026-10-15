package io.confluent.intellijplugin.toolwindow.actions

import io.confluent.intellijplugin.util.KafkaMessagesBundle
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.ide.actions.NewElementAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import io.confluent.intellijplugin.core.monitoring.toolwindow.MainTreeController.Companion.dataManager
import io.confluent.intellijplugin.core.monitoring.toolwindow.MainTreeController.Companion.rfsPath
import io.confluent.intellijplugin.core.rfs.driver.RfsPath
import io.confluent.intellijplugin.data.BaseClusterDataManager
import io.confluent.intellijplugin.util.KafkaDialogFactory

class CreateTopicAction : NewElementAction(), ActionPromoter, DumbAware {
    init {
        // a bare "+" next to "Producer"/"Consumer" didn't say what it creates
        templatePresentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val dataManager = e.dataManager as BaseClusterDataManager
        KafkaDialogFactory.showCreateTopicDialog(dataManager)
    }

    override fun update(e: AnActionEvent) {
        val rfsPath = e.rfsPath
        e.presentation.isEnabledAndVisible =
            e.dataManager is BaseClusterDataManager && rfsPath?.isTopicFolderOrChild() == true
        e.presentation.text = KafkaMessagesBundle.message(
            if (e.isFromActionToolbar) "action.kafka.CreateTopicAction.toolbar.text" else "action.kafka.CreateTopicAction.text"
        )
    }

    override fun promote(actions: List<AnAction>, context: DataContext): List<AnAction> {
        return listOf(this)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    private fun RfsPath.isTopicFolderOrChild(): Boolean {
        // Check if this path itself is a topic folder (Kafka: "Topics", CCloud: "lkc-*")
        val isFolder = this.name == "Topics" ||
                      (this.elements.size == 1 && this.name.startsWith("lkc-") && this.isDirectory)
        if (isFolder) return true

        // Check if parent is a topic folder
        val parentPath = this.parent ?: return false
        return parentPath.name == "Topics" ||
               (parentPath.elements.size == 1 && parentPath.name.startsWith("lkc-") && parentPath.isDirectory)
    }
}