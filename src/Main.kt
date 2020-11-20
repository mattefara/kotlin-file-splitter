import java.io.File

fun main() {

    val splitterSource = File( "files${File.separator}slides.pdf")
    val splitter = SplitterFactory(splitterSource,3 ).instantiate(Factory.Type.NORMAL_MERGER)
    splitter.split()

    val zipSplitter = SplitterFactory(splitterSource, 3, extensionName = "zip").instantiate(Factory.Type.ZIP_MERGER)
    zipSplitter.split()

    val password = "password"
    val cipheredSplitter = CipheredSplitterFactory(splitterSource, 3, password).instantiate(CipheredFactory.CipheredType.CIPHER_TYPE)
    cipheredSplitter.split()

//    val DIR = "files" + File.separator
//    val splitterSource = File(DIR + "slides.pdf")
//    val parts = 5
//
//    val splitterDestinations = MutableList(parts) {
//        File(DIR + "slides_part${it}.pdf.part")
//    }
//    val mergerDestination = File(DIR + "_slides.pdf")
//    val splitter = Splitter(splitterSource, splitterDestinations, splitterSource.length() / parts)
//    splitter.split()
//    val merger = Merger(splitterDestinations, mergerDestination)
//    merger.merge()
////    ------------------------------------------------------------------------------------------------------
//
//    val zipSplitterDestinations = MutableList(parts) {
//        File(DIR + "slides_part${it}.pdf.zip")
//    }
//    val zipMergerDestination = File(DIR + "_slides_unzip.pdf")
//    val zipSplitter = ZipSplitter(splitterSource, zipSplitterDestinations, splitterSource.length() / parts)
//    zipSplitter.split()
//    val zipMerger = ZipMerger(zipSplitterDestinations, zipMergerDestination)
//    zipMerger.merge()
//    //    ------------------------------------------------------------------------------------------------------
//
//    val cipheredSplitterDestinations = MutableList(parts) {
//        File(DIR + "slides_part${it}.pdf.crypt")
//    }
//    val cipheredMergerDestination = File(DIR + "_slides_deciphered.pdf")
//    val password = "password"
//    val cipheredSplitter = CipheredSplitter(splitterSource, cipheredSplitterDestinations, splitterSource.length() / parts, password)
//    cipheredSplitter.split()
//    val cipheredMerger = CipheredMerger(cipheredSplitterDestinations, cipheredMergerDestination, password)
//    cipheredMerger.merge()
//    //    ------------------------------------------------------------------------------------------------------

}