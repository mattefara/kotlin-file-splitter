import java.lang.Exception

interface Progress {
    fun onProgressUpdate(progress: Int, completed: Boolean) : Int
    fun onProgressReset()
    fun onProgressError()
    fun onProgressComplete()
}

abstract class ProgressThread : Thread(){
    val MAX_PROGRESS = 100
    val PROGRESS_RESET = -1
    val PROGRESS_ERROR = -2

    var progress = 0
        set(value) {
            field = listener.onProgressUpdate(value, value == MAX_PROGRESS)
        }

    private var listener : Progress = object : Progress {
        override fun onProgressUpdate(progress: Int, completed: Boolean): Int {
            println(progress)
            if (completed) {
                this.onProgressComplete()
            } else {
                when (progress) {
                    PROGRESS_ERROR -> {
                        this.onProgressError()
                    }
                    PROGRESS_RESET -> {
                        this.onProgressReset()
                    }
                }
            }
            return progress
        }

        override fun onProgressReset() {
            println("Progress reset!")
        }

        override fun onProgressError() {
            throw Exception("Error during splitting")
        }

        override fun onProgressComplete() {
            println("Complete!!")
        }

    }

    fun setProgressListener( listener: Progress ){
        this.listener = listener
    }

}
