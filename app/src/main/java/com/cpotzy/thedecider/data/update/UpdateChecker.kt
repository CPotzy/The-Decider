package com.cpotzy.thedecider.data.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val latestSha: String,
    val releaseUrl: String,
    val releaseName: String,
    val publishedAt: String?,
)

class UpdateChecker(
    private val owner: String,
    private val repo: String,
    private val currentSha: String,
    private val token: String = "",
) {
    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        val url = URL("https://api.github.com/repos/$owner/$repo/releases/latest")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "the-decider-app")
            if (token.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
            connectTimeout = 5_000
            readTimeout = 5_000
        }
        try {
            if (conn.responseCode !in 200..299) return@withContext null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)

            val htmlUrl = json.optString("html_url", "https://github.com/$owner/$repo/releases/latest")
            val name = json.optString("name", "Latest build")
            val publishedAt = json.optString("published_at").takeIf { it.isNotBlank() }
            val bodyText = json.optString("body", "")

            val latestSha = parseShaFromBody(bodyText) ?: json.optString("target_commitish").takeIf { it.length >= 7 }
            if (latestSha == null || currentSha == "unknown") return@withContext null
            if (matches(latestSha, currentSha)) return@withContext null

            UpdateInfo(
                latestSha = latestSha,
                releaseUrl = htmlUrl,
                releaseName = name,
                publishedAt = publishedAt,
            )
        } catch (e: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    private fun matches(latest: String, current: String): Boolean {
        val n = minOf(latest.length, current.length).coerceAtLeast(7)
        return latest.take(n).equals(current.take(n), ignoreCase = true)
    }

    companion object {
        private val SHA_REGEX = Regex("\\b[0-9a-f]{7,40}\\b", RegexOption.IGNORE_CASE)
        private fun parseShaFromBody(body: String): String? = SHA_REGEX.find(body)?.value
    }
}
