package com.mycompany.myapp.ui.main

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.mycompany.myapp.EspressoMatchers.regex
import com.mycompany.myapp.MainApplicationDaggerMockRule
import com.mycompany.myapp.R
import com.mycompany.myapp.data.api.github.GitHubInteractor
import com.mycompany.myapp.data.api.github.GitHubInteractor.LoadCommitsRequest
import com.mycompany.myapp.data.api.github.GitHubInteractor.LoadCommitsResponse
import com.mycompany.myapp.data.api.github.model.Author
import com.mycompany.myapp.data.api.github.model.Commit
import com.mycompany.myapp.data.api.github.model.CommitDetails
import com.mycompany.myapp.withRecyclerView
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @JvmField @Rule var mockitoRule = MainApplicationDaggerMockRule()

    @JvmField @Rule var testRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Mock lateinit var gitHubInteractor: GitHubInteractor

    @Test
    fun testBuildFingerprint() {
        whenever(gitHubInteractor.loadCommits(any())).thenReturn(Observable.empty())

        testRule.launchActivity(null)
        onView(withId(R.id.fingerprint)).check(matches(withText(regex("Fingerprint: .+"))))
    }

    @Test
    fun testFetchCommitsEnabledState() {
        val response = LoadCommitsResponse(
                LoadCommitsRequest("username", "repository"),
                emptyList())
        whenever(gitHubInteractor.loadCommits(any())).thenReturn(Observable.just(response))

        testRule.launchActivity(null)
        onView(withId(R.id.fetch_commits)).check(matches(isEnabled()))

        onView(withId(R.id.username)).perform(clearText())
        onView(withId(R.id.fetch_commits)).check(matches(not(isEnabled())))

        onView(withId(R.id.username)).perform(typeText("username"))
        onView(withId(R.id.fetch_commits)).check(matches(isEnabled()))

        onView(withId(R.id.repository)).perform(clearText())
        onView(withId(R.id.fetch_commits)).check(matches(not(isEnabled())))
    }

    @Test
    fun testFetchAndDisplayCommits() {
        val response = buildMockLoadCommitsResponse()
        whenever(gitHubInteractor.loadCommits(any())).thenReturn(response)

        testRule.launchActivity(null)
        closeSoftKeyboard()

        onView(withRecyclerView(R.id.commits)
                .atPositionOnView(0, R.id.author))
                .check(matches(withText("Author: Test author")))
        onView(withRecyclerView(R.id.commits)
                .atPositionOnView(0, R.id.message))
                .check(matches(withText("Test commit message")))
    }

    private fun buildMockLoadCommitsResponse(): Observable<LoadCommitsResponse> {
        val request = LoadCommitsRequest("madebyatomicrobot", "android-starter-project")
        val commit = Commit(CommitDetails("Test commit message", Author("Test author")))
        return Observable.just(LoadCommitsResponse(request, listOf(commit)))
    }
}
