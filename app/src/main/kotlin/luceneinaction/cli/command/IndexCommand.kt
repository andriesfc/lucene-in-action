package luceneinaction.cli.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.defaultLazy
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.unique
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import luceneinaction.cli.RuntimeContext
import luceneinaction.cli.UserMessage
import luceneinaction.commons.fs.toPath
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.KeywordField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.Term
import java.io.File
import kotlin.system.measureTimeMillis

class IndexCommand : CliktCommand(name = "index") {

    override fun help(context: Context): String = "Creates index from files."

    val docs by argument("source", help = "The source file, or directory to index")
        .path(mustExist = true)
        .defaultLazy { System.getProperty("user.dir").toPath() }

    val indexMode by option("--mode", "-M")
        .help("Index creation mode.")
        .choice(
            "create" to OpenMode.CREATE,
            "update" to OpenMode.CREATE_OR_APPEND
        ).default(OpenMode.CREATE_OR_APPEND, "update")

    val include by option("--include", "-I", metavar = "extension")
        .help("Which documents to include into the index")
        .multiple(default = listOf("txt", "html", "htm"))
        .unique()

    val includeEmpty by option("--include-empty")
        .flag(default = false)
        .help("Include empty docs in index")

    val forceMerge by option().flag(default = false)
        .help("Performs a full merge at the end for search optimization.")

    val deleteRemovedFiles by option()
        .help("Deletes removed files from the index.")
        .flag(default = false)

    private val ic by requireObject<RuntimeContext>()

    override fun run() {

        val config = IndexWriterConfig(StandardAnalyzer())
        config.commitOnClose = true
        config.openMode = indexMode

        val source = this@IndexCommand.docs.toFile()
        val onlyNonHidden: File.() -> Boolean = { !isHidden }
        val selection: File.() -> Boolean = {
            isFile && include.any { ext -> extension.equals(ext, ignoreCase = true) }
                    && (length() > 0 || includeEmpty)
        }

        ic.info("Creating index of $source into: ${ic.indexDir}")

        var docs = 0
        var duration: Long

        FSDirectory.open(ic.indexDir.toPath()).use { dir ->
            IndexWriter(dir, config).use { writer ->
                duration = measureTimeMillis {
                    when {
                        source.isFile -> writer.index(source, ic).also { ++docs }
                        else -> {
                            val root = source.path
                            source.walkTopDown()
                                .onEnter(onlyNonHidden)
                                .filter(selection)
                                .forEach { file ->
                                    writer.index(file, ic)
                                    ++docs
                                }
                        }
                    }
                }
                if (deleteRemovedFiles) writer.deleteRemoved()
                if (forceMerge) writer.forceMerge(1)
            }
        }

        val docsPerSecond = docs / duration * 1000
        ic.info("Completed: Processed $docs files in $duration ms ($docsPerSecond)")
    }

    private fun IndexWriter.index(file: File, user: UserMessage) {
        val doc = Document()
        doc.add(KeywordField("path", file.path, Field.Store.YES))
        doc.add(LongField("modified", file.lastModified(), Field.Store.YES))
        doc.add(TextField("contents", file.bufferedReader(Charsets.UTF_8)))
        when {
            config.openMode == OpenMode.CREATE -> addDocument(doc)
            else -> updateDocument(Term("path", file.path), doc)
        }
        user.debug("file: ${file.path}")
    }

    private fun IndexWriter.deleteRemoved() {
    }

}

