package jp.co.arsaga.extensions.repository.common

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