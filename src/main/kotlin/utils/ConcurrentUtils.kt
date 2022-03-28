package utils

import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


@OptIn(DelicateCoroutinesApi::class)
inline fun mainThread(crossinline block: () -> Unit) {
    GlobalScope.launch(Dispatchers.Main) {
        block()
    }
}

class SynchronizedTimer {

    companion object {
        private val executor = Executors.newFixedThreadPool(2)
    }

    private lateinit var onTick: (secondsPassed: Int, totalSeconds: Int) -> Unit
    private lateinit var onFinished: () -> Unit
    private var seconds: AtomicInteger = AtomicInteger(0)
    private var terminated: AtomicBoolean = AtomicBoolean(false)
    private val task: Runnable = Runnable {
        var passed = 0
        while (passed < seconds.get()) {
            passed++
            Thread.sleep(1000)
            synchronized(onTick) {
                if (!terminated.get()) {
                    onTick(passed, seconds.get())
                }
            }
            if (terminated.get()) {
                return@Runnable
            }
        }
        synchronized(onFinished) {
            if (!terminated.get()) {
                onFinished()
            }
        }
    }

    fun begin(
        seconds: Int,
        onTick: (secondsPassed: Int, totalSeconds: Int) -> Unit = { i: Int, i1: Int -> },
        onFinished: () -> Unit
    ) {
        this.onTick = onTick
        this.onFinished = onFinished
        this.seconds.set(seconds)
        executor.submit(task)
    }

    fun terminate() {
        terminated.set(true)
    }
}

