package config

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase

class StaticAppender : AppenderBase<ILoggingEvent>() {
    public override fun append(e: ILoggingEvent) {
        events.add(e)
    }

    companion object {
        var events: MutableList<ILoggingEvent> = ArrayList()

        fun clearEvents() {
            events.clear()
        }
    }
}
