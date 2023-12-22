package com.myvirtualab.intellij.loonar

import com.esotericsoftware.minlog.Log
import com.intellij.openapi.project.Project
import com.intellij.tasks.config.BaseRepositoryEditor
import com.intellij.ui.components.JBLabel
import com.intellij.util.Consumer
import com.intellij.util.ui.FormBuilder
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JComponent
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

    // login
    private lateinit var myLoginButton: JButton;

    init {
        myLoginButton.text = "Login"
        myLoginButton.isEnabled = false

        // hide default fields
        myUrlLabel.isVisible = false
        myURLText.isVisible = false
        myShareUrlCheckBox.isVisible = false
        // myUseProxy.isVisible = false
        myTestButton.isVisible = false

        updateTestButton()

        myLoginButton.addActionListener {
            val loginResponse = myRepository.loonarLogin()
            myLoginButton.text = "Logged as " + loginResponse.get("username").asString
        };

        if (myRepository.accessToken.isNotEmpty()) {
            val loginResponse = myRepository.loonarLogin()
            myLoginButton.text = "Logged as " + loginResponse.get("username").asString
        }

        val testUpdateListener =
                SimpleDocumentListener { e ->
                    myRepository.apiKey = myApiKeyText.text // apiKey
                    myRepository.username = myUserNameText.text // username
                    myRepository.password = myPasswordText.text // password
                    myRepository.userId = myUserIdText.text // user
                    myRepository.tag = myTagText.text // tag
                    myRepository.departmentId = myDepartmentIdText.text // department
                    updateTestButton()
                }

        myUserNameText.document.addDocumentListener(testUpdateListener)
        myPasswordText.document.addDocumentListener(testUpdateListener)
        myUserIdText.document.addDocumentListener(testUpdateListener)
        myTagText.document.addDocumentListener(testUpdateListener)
        myUserIdText.document.addDocumentListener(testUpdateListener)
        myDepartmentIdText.document.addDocumentListener(testUpdateListener)
    }

    override fun createCustomPanel(): JComponent? {
        myLoginButton = JButton()

        myTagLabel = JBLabel("Tag:")
        myTagText = JTextField(myRepository.tag)
        installListener(myTagText)

        myApiKeyLabel = JBLabel("API key:")
        myApiKeyText = JTextField(myRepository.apiKey)
        installListener(myApiKeyText)

        myUserIdLabel = JBLabel("User ID:")
        myUserIdText = JTextField(myRepository.userId)
        installListener(myUserIdText)

        myDepartmentIdLabel = JBLabel("Department ID:")
        myDepartmentIdText = JTextField(myRepository.departmentId)
        installListener(myDepartmentIdText)

        return FormBuilder.createFormBuilder()
                .addComponentToRightColumn(myLoginButton)
                // .addSeparator()
                // .addComponent(JBLabel("Board Filter"))
                .addLabeledComponent(myApiKeyLabel, myApiKeyText)
                .addLabeledComponent(myDepartmentIdLabel, myDepartmentIdText)
                .addLabeledComponent(myUserIdLabel, myUserIdText)
                .addLabeledComponent(myTagLabel, myTagText)
                .panel
    }

    override fun apply() {
        myRepository.tag = myTagText.text.trim()
        myRepository.apiKey = myApiKeyText.text.trim()
        myRepository.userId = myUserIdText.text.trim()
        myRepository.departmentId = myDepartmentIdText.text.trim()
        super.apply()
        updateTestButton()
    }

    private fun updateTestButton() {
        myLoginButton.isEnabled = myRepository.username.isNotBlank() && myRepository.password.isNotBlank()
        myTestButton.isEnabled = myRepository.isConfigured
    }
}
