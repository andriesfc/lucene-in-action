package luceneinaction.commons.clikt

import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.terminal.Terminal
import luceneinaction.cli.UserMessage

class UserMessageImpl(
    private val terminal: Terminal,
    private val verbose: Boolean
) : UserMessage {

    private val _info = terminal.theme.info
    private val _warning = terminal.theme.warning
    private val _error = terminal.theme.danger
    private val _debug = terminal.theme.info + brightYellow + bold
    private val _errorStackTrace = terminal.theme.danger + dim

    override fun info(message: String) {
        terminal.println(_info(message))

    }

    override fun warn(message: String) {
        terminal.println(_warning(message))
    }

    override fun error(message: String, cause: Throwable?) {
        terminal.println(_error(message))
        cause
            ?.stackTraceToString()
            ?.lineSequence()
            ?.map(_errorStackTrace::invoke)
            ?.forEach(terminal::println)
    }

    override fun debug(message: String) {
        if (verbose) terminal.println(_debug(message))
    }

}
