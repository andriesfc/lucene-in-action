package luceneinaction

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import luceneinaction.cli.RuntimeContext
import luceneinaction.cli.UserMessage
import luceneinaction.cli.command.IndexCommand
import luceneinaction.cli.command.SearchCommand
import luceneinaction.commons.clikt.UserMessageImpl
import luceneinaction.commons.fs.toFile
import java.io.File

fun main(args: Array<String>) {

    class ContextImpl(
        override val indexDir: File,
        messages: UserMessage
    ) : RuntimeContext, UserMessage by messages {}

    object : CliktCommand("luca") {

        override fun help(context: Context): String =
            "Lucene (in acton) tools."

        val indexDir by option("--index-dir", envvar = "LUCA_INDEX_DIR")
            .help("Directory where indexes are stored. ")
            .file(canBeFile = false, canBeSymlink = true)
            .defaultLazy("./local/index") { "./local/index".toFile() }

        val verbose by option("--verbose")
            .help("More noisy output")
            .flag(default = false)

        val context by findOrSetObject<RuntimeContext> {
            ContextImpl(
                indexDir,
                UserMessageImpl(currentContext.terminal, verbose)
            )
        }

        override val invokeWithoutSubcommand: Boolean = true

        init {

            configureContext {
                helpFormatter = {
                    MordantHelpFormatter(
                        it,
                        requiredOptionMarker = "*",
                        showRequiredTag = true,
                        showDefaultValues = true
                    )
                }
            }
        }

        override fun run() {
            context
        }

    }.subcommands(
        IndexCommand(),
        SearchCommand()
    ).main(args)

}
