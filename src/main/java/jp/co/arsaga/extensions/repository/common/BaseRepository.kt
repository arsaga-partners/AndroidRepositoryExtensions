package jp.co.arsaga.extensions.repository.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface BaseRepository<Store, Req> {
    val dataSource: Store
    fun refresh()
    fun fetch(request: Req?)
    fun isNeedUpdate(): Boolean = true
}

typealias BaseLiveDataRepository<T> = BaseRequestLiveDataRepository<T, Any?>

abstract class BaseRequestLiveDataRepository<T, Req>(initRequest: Req?) :
    BaseRepository<LiveData<T?>, Req> {

    protected val _dataSource by lazy {
        object : MutableLiveData<T?>(null) {
            override fun onActive() {
                super.onActive()
                if (isNeedUpdate()) refresh()
            }
        }
    }

    override val dataSource: LiveData<T?> = _dataSource

    private var latestRequestCache: Req? = initRequest

    override fun refresh() {
        fetch(latestRequestCache)
    }

    override fun fetch(request: Req?) {
        latestRequestCache = request
        dispatch(request)
    }

    protected abstract fun dispatch(request: Req?)

}