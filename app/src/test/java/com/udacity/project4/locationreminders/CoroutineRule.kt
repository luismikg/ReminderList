package com.udacity.project4.locationreminders

import kotlinx.coroutines.test.*
import org.junit.runner.*
import kotlinx.coroutines.*
import org.junit.rules.TestWatcher

@ExperimentalCoroutinesApi
class CoroutineRule(val testCoroutineDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) :
    TestWatcher(),
    TestCoroutineScope by TestCoroutineScope(testCoroutineDispatcher) {

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testCoroutineDispatcher)
    }
}