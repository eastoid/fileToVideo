package mogware

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.time.LocalDate
import java.util.*
import javax.imageio.ImageIO
import kotlin.system.exitProcess



fun main() {
    println("Do you want to encode or decode a file? E/D: ")
    val dorp = readLine().toString()
    if (dorp.contains("d")) {
        println("Decoding file:")
        decode()

    } else {
        println("Encoding file:")
        encode()
    }




}

fun encode() {
    println("Enter the full path of the file you want to fartalize: ")
    val fileInput = readLine().toString()
    println("$fileInput")

    val file = File(fileInput)
    val simpleFileName = "${file.nameWithoutExtension}.${file.extension}"
    var output = File("${fileInput.removeSuffix("$simpleFileName")}\\output")


    if (!file.exists()) {
        println("Your file does not exist.")
        exitProcess(69)
    }
    if (!output.exists()) {
        output.mkdir()
        println("created output directory: $output")
    } else {
        val current = LocalDate.now()
        val tempFile = File("${output}$current")
        tempFile.mkdir()
        output = tempFile
        println("created output directory: $output")
    }





    val size = getSize(file)
    val chunks = callGPTFunction(file)
    println(chunks)
    println("get sillilty trollerro. put inna qere c0da")
    println("This operation will generate: $size qr code images in the directory (QR file number calculator is insanely inacurate but still sorta works). Do you want to proceed? y/n: ")
    val input = readLine()
    if (input.toString().contains("y") || input.toString().contains("Y")) {
        println("Generating QR codes. This shit is not optimized whatsoever so it might take a while.")
        val metaName = File("$output\\")
        generateBlankImages(output.toString())
        generateMetaQR("fileName=$simpleFileName", "$output\\qr_code_7.png")
        generateQRCodes(chunks, output)
        println("Around $size codes should be generated.")

    } else {
        println("youre a pussy")
        exitProcess(235409784)
    }

    println("Will convert files into a video file using ffmpeg. continue? (will do it either way) (jk) y/y:")
    val input2 = readLine()
    if (input2 != "y") { exitProcess(6969) }


    runffmpeg(output, simpleFileName)

    println("Do you want to delete remaining QR code images? (Video file was generated.) Y/N")
    val delete = readLine().toString()
    if (delete.contains("y")) {

        deleteRemaing(output.toString())

    }


}


fun deleteRemaing(directoryPath: String) {
    val directory = File(directoryPath)
    val files = directory.listFiles()

    for (file in files) {
        if (file.isFile && file.name.contains("qr_code_") && file.name.contains(".png")) {
            file.delete()
        }
    }
}



//check filesize
fun getSize(file: File): String {
    val bytes: Long = Files.size(file.toPath())


    println(String.format("%,d bytes", bytes))
    println(String.format("%,d kilobytes", bytes / 1024))
    return String.format(("%,d "), bytes / 1024)

}




fun callGPTFunction(file: File): List<ByteArray> {

    val inputStream = FileInputStream(file)
    val chunks = chunkFileData(inputStream)
    return chunks
}

fun chunkFileData(inputStream: InputStream): List<ByteArray> {
    val chunkSize = 2 * 1024 // 2KB
    val chunks = mutableListOf<ByteArray>()
    var chunk = ByteArray(chunkSize)
    var bytesRead: Int
    while (inputStream.read(chunk, 0, chunkSize).also { bytesRead = it } != -1) {
        if (bytesRead == chunkSize) {
            chunks.add(chunk)
            chunk = ByteArray(chunkSize)
        } else {
            val lastChunk = ByteArray(bytesRead)
            System.arraycopy(chunk, 0, lastChunk, 0, bytesRead)
            chunks.add(lastChunk)
        }
    }
    return chunks
}


fun generateQRCodes(chunks: List<ByteArray>, input: File) {
    val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
    hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
    hints[EncodeHintType.CHARACTER_SET] = Charset.forName("UTF-8")
    val writer = QRCodeWriter()
    for ((index, chunk) in chunks.withIndex()) {
        val bitMatrix = writer.encode(String(chunk, Charsets.UTF_8), BarcodeFormat.QR_CODE, 600, 600, hints)
        // Create an image from the bit matrix
        val width = bitMatrix.width
        val height = bitMatrix.height
        val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
        for (x in 0 until width) {
            for (y in 0 until height) {
                image.setRGB(x, y, if (bitMatrix[x, y]) Color.BLACK.rgb else Color.WHITE.rgb)
            }
        }
        // Save the image as a PNG file

        val fileName = "qr_code_${index + 8}.png"
        val file = File("$input\\$fileName")
        ImageIO.write(image, "png", file)

        //ImageIO.write(image, "png", File("$input\\qr_code_${index + 1}.png"))
    }
}

fun generateMetaQR(input: String, fileName: String) {
    val size = 600
    val hintMap = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
        EncodeHintType.MARGIN to 2
    )
    val qrCodeWriter = QRCodeWriter()
    val bitMatrix = qrCodeWriter.encode(input, BarcodeFormat.QR_CODE, size, size, hintMap)
    val width = bitMatrix.width
    val image = BufferedImage(width, width, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until width) {
        for (y in 0 until width) {
            val color = if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            image.setRGB(x, y, color)
        }
    }
    ImageIO.write(image, "png", File(fileName))
}

fun generateBlankImages(directoryPath: String) {
    val directory = File(directoryPath)
    if (!directory.isDirectory) {
        throw IllegalArgumentException("$directoryPath is not a valid directory path")
    }

    val qrCodeName = directory.name

    for (i in 1..6) {
        val image = BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, 500, 500)
        graphics.dispose()

        val fileName = "qr_code_$i.png"
        val outputFile = File(directory, fileName)
        ImageIO.write(image, "png", outputFile)
    }
}