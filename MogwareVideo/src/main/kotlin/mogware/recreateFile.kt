package mogware

import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.DirectoryStream
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.system.exitProcess

fun decode() {
    // Prompt user for input file path
    print("Enter the path to your encoded video: ")
    val inputPath = readLine()

    // Check if input file exists
    val inputFile = File(inputPath)
    if (!inputFile.exists()) {
        println("Error: Input file does not exist.")
        return
    }

    if (inputPath == null) {
        exitProcess(9)
    }

    // Extract frames from video
    val inputDirectory = inputFile.parentFile
    splitVideoIntoFrames(inputPath, inputDirectory.path)
    println("Done extracting frames.")

    // Prompt user to convert QR frames to file
    print("Do you want to convert QR frames to file? (y/n): ")
    val convertFrames = readLine().toString().lowercase() == "y"
    if (!convertFrames) {
        return
    }

    // Check if input directory exists
    if (!inputDirectory.isDirectory || !inputDirectory.exists()) {
        println("Error: Input directory does not exist.")
        return
    }

    // Rename meta frame file
    val metaFrameFile = File("${inputDirectory.path}/metaFrame.png")
    val firstFrameFile = File("${inputDirectory.path}/frame000007.png")
    if (firstFrameFile.exists()) {
        firstFrameFile.renameTo(metaFrameFile)
        println("Renamed first frame to metaFrame.")
    } else {
        println("Error: First frame file does not exist.")
        return
    }

    // Find all frame files in input directory
    val frameFiles = inputDirectory.listFiles { file ->
        file.isFile && file.name.contains("frame") && file.name.endsWith(".png") && !file.name.contains("meta")
    }
    println("Processed ${frameFiles.size} files.")

    // Scan QR codes from frame files
    val qrCodes = scanQRCode(frameFiles.toList())
    println("Scanned ${qrCodes.size} QR codes.")

    // Scan metadata from meta frame file
    val metaData = scanMeta(metaFrameFile)
    val textMeta = String(metaData, Charsets.UTF_8)

    // Write QR code data to output file
    val outputPath = "${inputDirectory.path}/"
    writeByteArraysToFile(qrCodes, outputPath)
    println("Wrote QR code data to $outputPath.")
}



fun writeByteArraysToFile(byteArrays: List<ByteArray>, filePath: String) {
    var i = 0
    println("Did me go poopie ${i++}")

    val file = File(filePath)
    println("Did me go poopie ${i++}")

    val outputStream = FileOutputStream(file)
    println("Did me go poopie ${i++}")

    val bufferedOutputStream = BufferedOutputStream(outputStream)
    println("Did me go poopie ${i++}")

    byteArrays.forEach {
        bufferedOutputStream.write(it)
        println("Did me go poopie ${i++}")

    }
    println("Did me go poopie ${i++}")


    bufferedOutputStream.close()
    outputStream.close()
    println("Did me go poopie ${i++}")

}


fun scanQRCode(files: List<File>): List<ByteArray> {
    val results = mutableListOf<ByteArray>()
    val reader = MultiFormatReader()
    for (file in files) {
        val image = ImageIO.read(file)
        val source = BufferedImageLuminanceSource(image)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val result = reader.decode(bitmap)
        results.add(result.text.toByteArray())
    }
    return results
}

fun scanMeta(metaFrame: File): ByteArray {
    val reader = MultiFormatReader()
    val image = ImageIO.read(metaFrame)
    val source = BufferedImageLuminanceSource(image)
    val bitmap = BinaryBitmap(HybridBinarizer(source))
    val result = reader.decode(bitmap)
    return result.text.toByteArray()
}


fun splitVideoIntoFrames(videoFilePath: String, outputDirectoryPath: String) {
    val fPath = Paths.get("").toAbsolutePath().toString()
    val ffmpegPath = "$fPath\\src\\main\\kotlin\\mogware\\ffmpeg.exe"
    val command = "$ffmpegPath -i $videoFilePath -vf fps=60 $outputDirectoryPath/frame%06d.png"
    val process = Runtime.getRuntime().exec(command)

    process.waitFor()

    val inputStream = process.inputStream
    val buffer = ByteArray(1024)
    while (inputStream.read(buffer) != -1) {
        println(String(buffer))
    }

    File("$outputDirectoryPath/frame%06d.png").delete()
}

