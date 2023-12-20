package com.myvirtualab.intellij.loonar.models

import com.apollographql.apollo3.api.Fragment
import com.intellij.tasks.Comment
import com.intellij.tasks.Task
import com.intellij.tasks.TaskRepository
import com.intellij.tasks.TaskType
import com.intellij.util.containers.map2Array
import com.myvirtualab.intellij.loonar.LoonarRepository
import dev.mayankmkh.intellij.linear.apolloGenerated.fragment.ShortIssueConnection
import icons.LoonarPluginIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.swing.Icon

val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

public data class LoonarTaskNode(
        /**
         * The unique identifier of the entity.
         */
        public val id: String,
        /**
         * The issue's title.
         */
        public val titolo: String,
        /**
         * The issue's description in markdown format.
         */
        public val azione: String?,
        /**
         * The time at which the entity was created.
         */
        public val data_inizio: String,
        /**
         * The last time at which the entity was updated. This is the same as the creation time if the
         *     entity hasn't been update after creation.
         */
        public val data_mod: String,
        /**
         * The workflow state that the issue is associated with.
         */
        public val stato: Int,
        /**
         * Labels associated with this issue.
         *//*
        public val labels: ShortIssueConnection.Labels,
        *//**
         * Comments associated with the issue.
         *//*
        public val comments: ShortIssueConnection.Comments,*/
)

@SuppressWarnings("TooManyFunctions")
class LoonarTask(private val node: LoonarTaskNode, private val repository: LoonarRepository) : Task() {
    override fun getId(): String = node.id

    override fun getSummary(): String = node.titolo

    override fun getDescription(): String? = node.azione

    override fun getComments(): Array<Comment> {
        // return node.comments.nodes.map2Array { LoonarComment(it) }
        return arrayOf();
    }

    override fun getIcon(): Icon = LoonarPluginIcons.Logo

    override fun getType(): TaskType {
        /*node.labels.nodes.forEach {
            val taskType =
                when (it.name) {
                    "Feature" -> TaskType.FEATURE
                    "Bug" -> TaskType.BUG
                    "Improvement" -> TaskType.FEATURE
                    else -> null
                }
            if (taskType != null) return taskType
        }*/
        return TaskType.FEATURE
    }

    override fun getUpdated(): Date = format.parse(node.data_mod)

    override fun getCreated(): Date = format.parse(node.data_inizio)

    override fun isClosed(): Boolean = node.stato == 50 || node.stato == 100

    override fun isIssue(): Boolean = true

    override fun getIssueUrl(): String = ""

    override fun getRepository(): TaskRepository = repository
}
