package jp.co.arsaga.extensions.repository.common

import androidx.lifecycle.LiveData
import java.util.*

object CurrentTimeRepository : LiveData<Long>(System.currentTimeMillis()) {

    private const val ONE_SECOND_AT_MILLIS = 1000L

    private var timer: Timer? = null

    override fun onActive() {
        super.onActive()
        postValue(System.currentTimeMillis())
        timer?.cancel()
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    postValue(System.currentTimeMillis())
                }
            }, ONE_SECOND_AT_MILLIS, ONE_SECOND_AT_MILLIS)
        }
    }

    override fun onInactive() {
        timer?.cancel()
        timer = null
        super.onInactive()
    }

}