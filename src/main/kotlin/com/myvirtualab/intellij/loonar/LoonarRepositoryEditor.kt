package com.myvirtualab.intellij.loonar

import com.intellij.openapi.project.Project
import com.intellij.tasks.config.BaseRepositoryEditor
import com.intellij.ui.components.JBLabel
import com.intellij.util.Consumer
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPasswordField
import javax.swing.JTextField

class LoonarRepositoryEditor(
        project: Project,
        linearRepository: LoonarRepository,
        changeListener: Consumer<in LoonarRepository>,
) : BaseRepositoryEditor<LoonarRepository>(project, linearRepository, changeListener) {
    private lateinit var myTagLabel: JBLabel
    private lateinit var myTagText: JTextField
    private lateinit var myApiKeyLabel: JBLabel
    private lateinit var myApiKeyText: JTextField
    private lateinit var myUserIdLabel: JBLabel
    private lateinit var myUserIdText: JTextField
    private lateinit var myDepartmentIdLabel: JBLabel
    private lateinit var myDepartmentIdText: JTextField

    init {
        // myPasswordLabel.text = "API key:"
        // myUsernameLabel.text = "Department ID:"
        // myUrlLabel.text = "User ID:"

        // hide default fields
        myUrlLabel.isVisible = false
        myURLText.isVisible = false
        myPasswordText.isVisible = false
        myPasswordLabel.isVisible = false
        myShareUrlCheckBox.isVisible = false
        myUsernameLabel.isVisible = false
        myUserNameText.isVisible = false

        updateTestButton()

        val testUpdateListener =
                SimpleDocumentListener { e ->
                    myRepository.password = myApiKeyText.text // apiKey
                    myPasswordText.text = myApiKeyText.text // apiKey
                    myRepository.userId = myUserIdText.text // user
                    myRepository.tag = myTagText.text // tag
                    myRepository.departmentId = myDepartmentIdText.text // department
                    updateTestButton()
                }
        myUserNameText.document.addDocumentListener(testUpdateListener)
        myUserIdText.document.addDocumentListener(testUpdateListener)
        myTagText.document.addDocumentListener(testUpdateListener)
        myUserIdText.document.addDocumentListener(testUpdateListener)
        myDepartmentIdText.document.addDocumentListener(testUpdateListener)
    }

    override fun createCustomPanel(): JComponent? {
        myTagLabel = JBLabel("Tag:")
        myTagText = JTextField(myRepository.tag)
        installListener(myTagText)

        myApiKeyLabel = JBLabel("API key:")
        myApiKeyText = JTextField(myRepository.password)
        installListener(myApiKeyText)

        myUserIdLabel = JBLabel("User ID:")
        myUserIdText = JTextField(myRepository.userId)
        installListener(myUserIdText)

        myDepartmentIdLabel = JBLabel("Department ID:")
        myDepartmentIdText = JTextField(myRepository.departmentId)
        installListener(myDepartmentIdText)

        return FormBuilder.createFormBuilder()
                .addLabeledComponent(myApiKeyLabel, myApiKeyText)
                .addLabeledComponent(myDepartmentIdLabel, myDepartmentIdText)
                .addLabeledComponent(myUserIdLabel, myUserIdText)
                .addLabeledComponent(myTagLabel, myTagText)
                .panel
    }

    override fun apply() {
        myRepository.tag = myTagText.text.trim()
        myRepository.password = myApiKeyText.text.trim()
        myRepository.userId = myUserIdText.text.trim()
        myRepository.departmentId = myDepartmentIdText.text.trim()
        super.apply()
        updateTestButton()
    }

    private fun updateTestButton() {
        myTestButton.isEnabled = myRepository.isConfigured
    }
}
