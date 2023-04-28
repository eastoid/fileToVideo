package mogware

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths


// efef mepeg !!

fun runffmpeg(path: File, simpleFileName: String) {

    val fPath = Paths.get("").toAbsolutePath().toString()


    val ffmpegCommand = listOf(
        "${fPath}\\src\\main\\kotlin\\mogware\\ffmpeg.exe",
        "-framerate", "60",
        "-i", "${path.absolutePath}\\qr_code_%d.png",
        "-s", "600x600",
        "-b:v", "3000000",
        "-c:v", "libx264",
        "-vf", "fps=60",
        "-pix_fmt", "yuv420p",
        "${path}\\${simpleFileName}-converted.mp4"
    )


    val processBuilder = ProcessBuilder(ffmpegCommand)

    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()

    val inputStream = process.inputStream
    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
    var line: String?
    while (bufferedReader.readLine().also { line = it } != null) {
        println(line)
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        println("Error occurred during ffmpeg conversion.")
    } else {
        println("Conversion successful.")
    }
}
