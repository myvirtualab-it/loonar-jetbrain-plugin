package com.myvirtualab.intellij.loonar

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.LOG
import com.intellij.credentialStore.generateServiceName
import com.intellij.tasks.CustomTaskState
import com.intellij.tasks.Task
import com.intellij.tasks.impl.BaseRepository
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl
import com.intellij.util.UriUtil
import com.intellij.util.containers.map2Array
import com.intellij.util.xmlb.annotations.Tag
import com.myvirtualab.intellij.loonar.models.LoonarTask
import com.myvirtualab.intellij.loonar.models.LoonarTaskNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request


@Suppress("TooManyFunctions")
@Tag("Linear")
class LoonarRepository : NewBaseRepositoryImpl {
    var departmentId: String = ""
    var userId: String = ""
    var tag: String = ""

    private val apiKeyProvider = ApiKeyProvider { password }
    private val remoteDataSource = LoonarRemoteDataSource(createApolloClient(API_URL, apiKeyProvider))

    /**
     * Serialization constructor
     */
    @Suppress("unused")
    constructor() : super()

    constructor(type: LoonarRepositoryType) : super(type) {
        url = "https://be.loonar.it/api/v1/tasks/"
        username = "Loonar"
    }

    constructor(other: LoonarRepository) : super(other) {
        departmentId = other.departmentId
        userId = other.userId
        tag = other.tag
        // password = other.password
    }

    override fun clone(): BaseRepository = LoonarRepository(this)

    /*override fun getUrl(): String {
        return this.getPresentableName();
    }*/

    override fun findTask(id: String): Task? {
        // TODO("Not yet implemented")
        return null
    }

    override fun getPresentableName(): String {
        val name = super.getPresentableName()
        // return name + "/" + workspaceId.ifEmpty { "{workspaceId}" } + "/team/${getTeamId()}"
        // &access_token=BLa4KjczcYWoZKOwxW5vRdDSPq8SvlGvLHqoCQtkgvW9D2b43ZCm36QZi6SFkDvgJXH5XIFyHN12w7vI
        LOG.info("password: " + password);
        var finalUrl: String = name.plus("?page=1&limit=100");
        if (tag.isNotEmpty()) finalUrl = finalUrl.plus("&tag=" + tag)
        if (departmentId.isNotEmpty()) finalUrl = finalUrl.plus("&departmentId=" + departmentId)
        if (userId.isNotEmpty()) finalUrl = finalUrl.plus("&userId=" + userId)
        return finalUrl
    }

    override fun isConfigured(): Boolean =
        super.isConfigured() && password.isNotBlank() && (tag.isNotBlank() || userId.isNotBlank() || departmentId.isNotBlank())

    override fun getIssues(
        query: String?,
        offset: Int,
        limit: Int,
        withClosed: Boolean,
    ): Array<LoonarTask> {
        // val issues = remoteDataSource.getIssues(username, query, offset, limit, withClosed)
        val client = OkHttpClient()
        val request: Request = Request.Builder()
                .url(this.getPresentableName())
                .addHeader("Authorization", "Bearer $password")
                .build()

        client.newCall(request).execute().use { response ->
            val gson = Gson()
            val listType = object : TypeToken<List<LoonarTaskNode>>() {}.type
            return gson.fromJson<Array<LoonarTaskNode>?>(response.body?.string(), listType).map2Array { LoonarTask(it, this) }
        }
        // return issues.map2Array { LoonarTask(it, this) }
    }

    override fun createCancellableConnection(): CancellableConnection {
        return object : CancellableConnection() {
            private var testJob: Job = Job()

            override fun doTest() {
                testJob = Job()
                runBlocking(testJob) {
                    // TODO sistemare
                    // remoteDataSource.testConnection(username)
                }
            }

            override fun cancel() {
                testJob.cancel()
            }
        }
    }

    override fun getFeatures(): Int {
        return super.getFeatures() or STATE_UPDATING
    }

    override fun getAvailableTaskStates(task: Task): MutableSet<CustomTaskState> {
        return runBlocking { remoteDataSource.getAvailableTaskStates(task) }
    }

    override fun setTaskState(
        task: Task,
        state: CustomTaskState,
    ) {
        runBlocking { remoteDataSource.setTaskState(task, state) }
    }

    override fun getAttributes(): CredentialAttributes {
        val serviceName = generateServiceName("Tasks", repositoryType.name + " " + getPresentableName())
        return CredentialAttributes(serviceName, username)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as LoonarRepository

        return departmentId == other.departmentId
    }

    override fun hashCode(): Int {
        return this.getPresentableName().hashCode()
    }

    companion object {
        private const val API_URL = "https://be.loonar.it/api/v1/tasks"
    }
}
