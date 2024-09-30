package org.prasi.sharedModule.eventbus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object FlowEventBus {
    private val mEvents = MutableSharedFlow<EventInterface>()
    val events = mEvents.asSharedFlow()

    suspend fun publishEvent(event: EventInterface) {
        mEvents.emit(event)
    }
}
