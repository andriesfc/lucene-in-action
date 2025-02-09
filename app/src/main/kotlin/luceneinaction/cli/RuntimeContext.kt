package luceneinaction.cli

import java.io.File

interface RuntimeContext: UserMessage {
    val indexDir: File
}

