package luceneinaction.commons.fs

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

fun String.toPath(): Path =
    Paths.get(this).run { if (exists()) normalize().toRealPath() else this }


fun String.toFile(): File = toPath().toFile()
