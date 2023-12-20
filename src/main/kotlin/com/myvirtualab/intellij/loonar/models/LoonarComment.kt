package com.myvirtualab.intellij.loonar.models

import com.intellij.tasks.Comment
import dev.mayankmkh.intellij.linear.apolloGenerated.fragment.ShortIssueConnection
import java.util.Date

class LoonarComment(private val node: ShortIssueConnection.Node2) : Comment() {
    override fun getText(): String = node.body

    override fun getAuthor(): String = node.user.name

    override fun getDate(): Date = node.createdAt
}
