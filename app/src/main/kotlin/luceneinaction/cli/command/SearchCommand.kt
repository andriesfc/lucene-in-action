package luceneinaction.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

class SearchCommand : CliktCommand("search") {

    override fun help(context: Context): String = "Search index"

    override fun run() {}
}
