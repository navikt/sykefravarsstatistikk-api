package common

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class StaticAppenderExtension : BeforeEachCallback {
    override fun beforeEach(context: ExtensionContext) {
        StaticAppender.clearEvents()
    }
}