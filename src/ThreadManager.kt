class ThreadManager<T : Thread> {

    private val queue = HashMap<Long, T>()

    fun startAll(){
        queue.forEach { (_, thread) -> thread.start() }
    }

    fun start(id: Long){
        queue[id]?.start()
    }

}
