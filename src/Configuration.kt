import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec

interface Split{
    fun split()
}

interface Merge{
    fun merge()
}

interface Stream<I : InputStream, O : OutputStream>{
    fun getInputStream(file: File) : I
    fun getOutputStream(file: File) : O
}

abstract class Configuration(val source: MutableList<File>, val destination: File, val BLOCK_SIZE: Int = 8 * 1024) : ProgressThread(), Stream<InputStream, OutputStream>

open class Splitter(source: File, destination: MutableList<File>, val size: Long, BLOCK_SIZE: Int = 8 * 1024)
    : Configuration(destination, source, BLOCK_SIZE), Split{

    override fun run() {
        super.run()
        split()
    }

    override fun split() {
        val inputStream = getInputStream(destination)
        for (i in source.indices){
            val bytes = ByteArray(BLOCK_SIZE)
            val outputStream = getOutputStream(source[i])
            var read = 0
            var length: Int
            while( inputStream.available() > 0 && size - read > 0 ){
                length = inputStream.read(bytes)
                outputStream.write(bytes, 0, length)
                read += length
            }
            progress = (((i * size + read) / destination.length().toFloat()) * 100).toInt()
            outputStream.close()
        }
        progress = MAX_PROGRESS
        inputStream.close()
    }


    override fun getInputStream(file: File): InputStream {
        return FileInputStream(file)
    }

    override fun getOutputStream(file: File): OutputStream {
        return FileOutputStream(file)
    }
}

open class Merger(source: MutableList<File>, destination: File, BLOCK_SIZE: Int = 8 * 1024)
    : Configuration(source, destination, BLOCK_SIZE), Merge {

    override fun merge() {
        val totalToMerge = source[0].length() * (source.size - 1) + source[source.size - 1].length()
        val outputStream = getOutputStream(destination)
        var total = 0
        for ( i in source.indices){
            val bytes = ByteArray(BLOCK_SIZE)
            val inputStream = getInputStream(source[i])
            while (inputStream.available() >= 0){
                val length = inputStream.read(bytes)
                if (length <= 0) break
                outputStream.write(bytes, 0, length)
                total += length
            }
            progress = (((total / totalToMerge.toDouble())) * 100).toInt()
            inputStream.close()
        }
        outputStream.close()
    }

    override fun getInputStream(file: File): InputStream {
        return FileInputStream(file)
    }
    override fun getOutputStream(file: File): OutputStream {
        return FileOutputStream(file)
    }
}

class ZipSplitter(source: File, destination: MutableList<File>, size: Long, BLOCK_SIZE: Int = 8 * 1024)
    : Splitter(source, destination, size, BLOCK_SIZE){

    override fun getOutputStream(file: File): OutputStream {
        return ZipOutputStream(super.getOutputStream(file)).apply {
            val zipEntry = ZipEntry(file.name.substring(0, file.name.lastIndexOf('.')))
            this.setLevel(4)
            this.putNextEntry(zipEntry)
        }
    }
}

class ZipMerger(source: MutableList<File>, destination: File, BLOCK_SIZE: Int = 8 * 1024)
    : Merger(source, destination, BLOCK_SIZE){
    override fun getInputStream(file: File): InputStream {
        return ZipInputStream(super.getInputStream(file)).also {
            val entry = it.nextEntry
            print(entry.name + " " + entry.size)
        }
    }
}

class CipheredSplitter(source: File, destination: MutableList<File>, size: Long, private val password: String, BLOCK_SIZE: Int = 8 * 1024)
    : Splitter(source, destination, size, BLOCK_SIZE) {

    private val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")!!

    override fun getOutputStream(file: File): OutputStream {
        val stream = super.getOutputStream(file)
        val salt = KeyGenerator.generateSalt()

        cipher.generateCryptCipher(password, salt)
        val iv = cipher.parameters.getParameterSpec(IvParameterSpec::class.java).iv

        stream.write(iv)
        stream.write(salt)

        return CipherOutputStream(stream, cipher)
    }

}

class CipheredMerger(source: MutableList<File>, destination: File, private val password: String, BLOCK_SIZE: Int = 8 * 1024)
    : Merger(source, destination, BLOCK_SIZE){

    private val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")!!

    override fun getInputStream(file: File): InputStream {
        val stream = super.getInputStream(file)
        val iv = ByteArray(16)
        val salt = ByteArray(16)

        stream.read(iv)
        stream.read(salt)

        cipher.generateDecryptCipher(password, iv, salt)
        return CipherInputStream(stream, cipher)
    }
}



