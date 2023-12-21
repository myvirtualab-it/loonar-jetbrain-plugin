package com.myvirtualab.intellij.loonar

import com.esotericsoftware.minlog.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.intellij.credentialStore.LOG
import com.intellij.tasks.CustomTaskState
import com.intellij.tasks.Task
import com.intellij.tasks.impl.BaseRepository
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl
import com.intellij.util.containers.map2Array
import com.intellij.util.xmlb.annotations.Tag
import com.myvirtualab.intellij.loonar.models.LoginParams
import com.myvirtualab.intellij.loonar.models.LoonarTask
import com.myvirtualab.intellij.loonar.models.LoonarTaskNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


@Suppress("TooManyFunctions")
@Tag("Linear")
class LoonarRepository : NewBaseRepositoryImpl {
    var departmentId: String = ""
    var userId: String = ""
    var tag: String = ""
    var apiKey: String = ""
    var accessToken: String = ""

    /**
     * Serialization constructor
     */
    @Suppress("unused")
    constructor() : super()

    constructor(type: LoonarRepositoryType) : super(type) {
        url = "https://be.loonar.it/api/v1/tasks/"
    }

    constructor(other: LoonarRepository) : super(other) {
        departmentId = other.departmentId
        userId = other.userId
        tag = other.tag
        apiKey = other.apiKey
        accessToken = other.accessToken
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
        LOG.info("password: " + password);
        var finalUrl: String = name.plus("?page=<%page%>&limit=<%limit%>");
        if (tag.isNotEmpty()) finalUrl = finalUrl.plus("&tag=" + tag)
        if (departmentId.isNotEmpty()) finalUrl = finalUrl.plus("&departmentId=" + departmentId)
        if (userId.isNotEmpty()) finalUrl = finalUrl.plus("&userId=" + userId)
        return finalUrl
    }

    override fun isConfigured(): Boolean =
            super.isConfigured() &&
                    username.isNotBlank() &&
                    password.isNotBlank() &&
                    (tag.isNotBlank() || userId.isNotBlank() || departmentId.isNotBlank()) &&
                    accessToken.isNotBlank()

    override fun getIssues(
            query: String?,
            offset: Int,
            limit: Int,
            withClosed: Boolean,
    ): Array<LoonarTask> {
        // val issues = remoteDataSource.getIssues(username, query, offset, limit, withClosed)
        val client = OkHttpClient()
        // Log.info("$offset $limit");
        val request: Request = Request.Builder()
                .url(this.getPresentableName()
                        .replace("<%page%>", (offset + 1).toString())
                        .replace("<%limit%>", limit.toString()))
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

        client.newCall(request).execute().use { response ->
            // Log.info(response.body?.string());
            val gson = Gson()
            val listType = object : TypeToken<List<LoonarTaskNode>>() {}.type
            return gson.fromJson<List<LoonarTaskNode>?>(response.body?.string(), listType).map2Array { LoonarTask(it, this) }
        }
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

    override fun getPreferredOpenTaskState(): CustomTaskState {
        return CustomTaskState(20.toString(), "Wip");
    }

    override fun getPreferredCloseTaskState(): CustomTaskState {
        return CustomTaskState(50.toString(), "Chiuso");
    }

    override fun getFeatures(): Int {
        return super.getFeatures() or STATE_UPDATING
    }

    override fun getAvailableTaskStates(task: Task): MutableSet<CustomTaskState> {
        val stateSet: MutableSet<CustomTaskState> = mutableSetOf();
        stateSet.add(CustomTaskState(50.toString(), "Chiuso"))
        stateSet.add(CustomTaskState(20.toString(), "Wip"))
        stateSet.add(CustomTaskState(10.toString(), "Aperto"))
        return stateSet;
    }

    override fun setTaskState(
            task: Task,
            state: CustomTaskState,
    ) {
        Log.info(task.id)
        Log.info(state.id)


        // Loonar login
        // val loonarToken: String = this.loonarLogin();
        // Log.info("token: $loonarToken")

        // Update task status
        val formUpdateTaskBody = FormBody.Builder()
                .add("state", state.id)
                .add("note", "Task closed from JetBrains IDE")
                .build()
        val updateTaskRequest: Request = Request.Builder()
                .url(API_URL.plus("/").plus(task.id))
                .put(formUpdateTaskBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("X-Token", accessToken)
                .build()
        val client = OkHttpClient()
        client.newCall(updateTaskRequest).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            println(response.body?.string())
        }
        // runBlocking { remoteDataSource.setTaskState(task, state) }
    }

    /*override fun getAttributes(): CredentialAttributes {
        val serviceName = generateServiceName("Tasks", repositoryType.name + " " + getPresentableName())
        return CredentialAttributes(serviceName, username)
    }*/

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

    public fun loonarLogin(): String {
        val formLoonarLoginBody = Gson().toJson(LoginParams(username, password));
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = formLoonarLoginBody.toRequestBody(mediaType)

        val loonarLoginRequest: Request = Request.Builder()
                .url(BASE_URL.plus("/api/v1/web/login"))
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "8F39D289C2D21ABA1D95845FF5F26BE1")
                .build()
        val client = OkHttpClient()
        client.newCall(loonarLoginRequest).execute().use { response ->
            val rawResponse = response.body?.string()
            Log.info(rawResponse)
            val loginResponse = JsonParser.parseString(rawResponse).asJsonObject
            return loginResponse.get("token").asString
        }
    }

    companion object {
        private const val BASE_URL = "https://be.loonar.it"
        private const val API_URL = "https://be.loonar.it/api/v1/tasks"
    }
}
