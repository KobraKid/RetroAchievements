package com.kobrakid.retroachievements

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors private constructor(private val diskIO: Executor, private val networkIO: Executor, private val mainThread: Executor) {
    fun diskIO(): Executor {
        return diskIO
    }

    fun mainThread(): Executor {
        return mainThread
    }

    fun networkIO(): Executor {
        return networkIO
    }

    private class DatabaseThreadExecutor : Executor {
        private val handler = Handler(Looper.getMainLooper())
        override fun execute(runnable: Runnable) {
            handler.post(runnable)
        }
    }

    companion object {
        private val LOCK = Any()

        @JvmStatic
        var instance: AppExecutors? = null
            get() {
                if (field == null) {
                    synchronized(LOCK) {
                        field = AppExecutors(
                                Executors.newSingleThreadExecutor(),
                                Executors.newFixedThreadPool(3),
                                DatabaseThreadExecutor())
                    }
                }
                return field
            }
            private set
    }

}