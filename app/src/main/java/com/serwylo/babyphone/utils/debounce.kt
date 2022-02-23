package com.serwylo.babyphone.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Constructs a function that processes input data and passes it to [destinationFunction]only if
 * there's no new data for at least [waitMs]
 *
 * From https://gist.github.com/Terenfear/a84863be501d3399889455f391eeefe5.
 */
fun <T> debounce(
    waitMs: Long = 300L,
    coroutineScope: CoroutineScope,
    destinationFunction: (T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(waitMs)
            destinationFunction(param)
        }
    }
}
