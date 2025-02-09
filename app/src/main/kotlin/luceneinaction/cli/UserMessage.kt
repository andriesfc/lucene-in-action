package luceneinaction.cli

interface UserMessage {
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String, cause: Throwable? = null)
    fun debug(message: String)
}
