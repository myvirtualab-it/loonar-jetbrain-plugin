package com.myvirtualab.intellij.loonar

import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskRepository
import com.intellij.tasks.config.TaskRepositoryEditor
import com.intellij.tasks.impl.BaseRepositoryType
import com.intellij.util.Consumer
import icons.LoonarPluginIcons
import javax.swing.Icon

class LoonarRepositoryType : BaseRepositoryType<LoonarRepository>() {
    override fun getName(): String = "Loonar"

    override fun getIcon(): Icon = LoonarPluginIcons.Logo

    override fun createRepository(): TaskRepository = LoonarRepository(this)

    override fun getRepositoryClass(): Class<LoonarRepository> = LoonarRepository::class.java

    override fun createEditor(
            repository: LoonarRepository,
            project: Project,
            changeListener: Consumer<in LoonarRepository>,
    ): TaskRepositoryEditor {
        return LoonarRepositoryEditor(project, repository, changeListener)
    }
}
