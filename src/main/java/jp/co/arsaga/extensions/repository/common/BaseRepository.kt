package jp.co.arsaga.extensions.repository.common

import java.util.concurrent.atomic.AtomicBoolean

interface BaseRepository<Store, Req> {
    val dataSource: Store
    fun refresh()
    fun fetch(request: Req?)
    fun isNeedUpdate(): Boolean = true

    abstract class Impl<Res, Store, Req>(initRequest: Req?) : BaseRepository<Store, Req> {

        internal var latestRequestCache: Req? = initRequest
            private set

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

interface BasePagingRepository<Store, Req> : BaseRepository<Store, Req> {

    val isBusy: AtomicBoolean

    fun fetchNextPage()

    fun release()

    fun isTerminal(): Boolean

    abstract class Impl<Res, Store, Req, Content>(
        initRequest: Req?
    ) : BaseRepository.Impl<Res, Store, Req>(initRequest), BasePagingRepository<Store, Req> {

        abstract fun limitEntityCount(): Int

        abstract fun currentList(): Collection<Content>

        abstract fun combineList(currentList: Collection<Content>, response: Res): Res

        abstract fun nextRequestFactory(offset: Int, latestRequest: Req?): Req?

        override val isBusy: AtomicBoolean = AtomicBoolean(false)

        override fun fetch(request: Req?) {
            if (!isTerminal() && isBusy.compareAndSet(false, true)) {
                super.fetch(request)
            }
        }

        override fun fetchNextPage() {
            nextRequestFactory(currentList().size.plus(1), latestRequestCache)
                ?.run(::fetch)
        }

        override fun release() {
            isBusy.set(false)
        }
    }
}