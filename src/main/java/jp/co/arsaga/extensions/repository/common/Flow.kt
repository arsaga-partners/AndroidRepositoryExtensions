package jp.co.arsaga.extensions.repository.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun <T>Flow<T>.stateInSwitchThread(
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher
): StateFlow<T?> = MutableStateFlow<T?>(null).also { stateFlow ->
    coroutineScope.launch(dispatcher) {
        collect {
            stateFlow.value = it
        }
    }
}