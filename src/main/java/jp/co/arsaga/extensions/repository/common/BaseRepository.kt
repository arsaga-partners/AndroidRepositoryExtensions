package jp.co.arsaga.extensions.repository.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface BaseRepository<Store, Req> {
    val dataSource: Store
    fun refresh()
    fun fetch(request: Req?)
    fun isNeedUpdate(): Boolean = true

    abstract class Impl<Res, Store, Req>(initRequest: Req?) : BaseRepository<Store, Req> {

        private var latestRequestCache: Req? = initRequest

        override fun refresh() {
            dispatch(latestRequestCache)
        }

        override fun fetch(request: Req?) {
            latestRequestCache = requestCacheFactory(request)
            dispatch(request)
        }

        protected open fun requestCacheFactory(request: Req?) = request

        protected abstract fun dataPush(response: Res?)

        protected abstract fun dispatch(request: Req?)
    }
}

typealias BaseLiveDataRepository<T> = BaseRequestLiveDataRepository<T, Any?>

abstract class BaseRequestLiveDataRepository<T, Req>(initRequest: Req?) :
    BaseRepository.Impl<T, LiveData<T?>, Req>(initRequest) {

    private val _dataSource by lazy {
        object : MutableLiveData<T?>(null) {
            override fun onActive() {
                super.onActive()
                if (isNeedUpdate()) refresh()
            }
        }
    }

    override val dataSource: LiveData<T?> = _dataSource

    override fun dataPush(response: T?) {
        _dataSource.postValue(response)
    }
}