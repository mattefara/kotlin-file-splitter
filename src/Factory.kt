import java.io.File
import java.nio.file.Path

interface Factory{
    object Type {
        const val NORMAL_MERGER = 0
        const val ZIP_MERGER = 1
    }
    fun instantiate(type: Int) : Configuration

}

interface CipheredFactory : Factory {
    object CipheredType{
        const val CIPHER_TYPE = 3
    }
}


open class SplitterFactory(val source: File, val parts: Int, var destinationDirectory: Path? = null, private val extensionName: String = "part", val BLOCK_SIZE: Int = 8 * 1024) : Factory {

    val destinations : MutableList<File>

    init {
        if (destinationDirectory == null) {
            destinationDirectory = File("${source.parent}${File.separator}${source.nameWithoutExtension}_$extensionName").toPath()
        }
        if (!File(destinationDirectory!!.toUri()).mkdir()) {
            println("Errore durante la creazione di ${destinationDirectory!!}")
        }
        destinations = MutableList(parts) {
            File("$destinationDirectory${File.separator}${source.nameWithoutExtension}_part$it.${source.extension}.$extensionName")
        }
    }

    override fun instantiate(type: Int) : Splitter{
        return when (type) {
            Factory.Type.NORMAL_MERGER -> Splitter(source, destinations, source.length() / parts, BLOCK_SIZE)
            Factory.Type.ZIP_MERGER -> ZipSplitter(source,destinations, source.length() / parts, BLOCK_SIZE)
            else -> throw Exception("Cannot instantiate merger type")
        }
    }
}

open class MergerFactory(val source: MutableList<File>, val destination: File, val BLOCK_SIZE: Int = 8 * 1024) : Factory {

    override fun instantiate(type: Int) : Merger{
        return when (type) {
            Factory.Type.NORMAL_MERGER -> Merger(source, destination, BLOCK_SIZE)
            Factory.Type.ZIP_MERGER -> ZipMerger(source,destination, BLOCK_SIZE)
            else -> throw Exception("Cannot instantiate merger type")
        }
    }
}

class CipheredSplitterFactory(source: File,  parts: Int, private val password : String, destination: Path? = null, extensionName : String = "crypt", BLOCK_SIZE: Int = 8 * 1024 ) :
        SplitterFactory(source, parts, destination, extensionName, BLOCK_SIZE ) {

    override fun instantiate(type : Int) : Splitter{
        return when(type) {
            CipheredFactory.CipheredType.CIPHER_TYPE -> CipheredSplitter(source,destinations, source.length() / parts ,password, BLOCK_SIZE)
            else -> super.instantiate(type)
        }
    }

}

class CipheredMergerFactory(source: MutableList<File>, destination: File, private val password: String, BLOCK_SIZE: Int = 8 * 1024,)
    : MergerFactory(source, destination, BLOCK_SIZE){

    override fun instantiate(type : Int) : Merger{
        return when(type) {
            CipheredFactory.CipheredType.CIPHER_TYPE -> CipheredMerger(source,destination, password, BLOCK_SIZE)
            else -> super.instantiate(type)
        }
    }
}

