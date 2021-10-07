package jp.co.arsaga.extensions.repository.common

import java.util.concurrent.atomic.AtomicBoolean

interface BaseRepository<Store, Req> {
    val dataSource: Store
    val requestQuery: (() -> Req)?
    fun refresh()
    fun fetch(request: Req?)
    fun isNeedUpdate(): Boolean = true

    abstract class Impl<Res, Store, Req>(
        override val requestQuery: (() -> Req)?
    ) : BaseRepository<Store, Req> {

        override fun fetch(request: Req?) {
            dispatch(request)
        }

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
        override val requestQuery: (() -> Req)?
    ) : BaseRepository.Impl<Res, Store, Req>(requestQuery), BasePagingRepository<Store, Req> {

        protected var latestRequestCache: Req? = requestQuery?.invoke()
            private set

        protected open fun requestCacheFactory(request: Req?) = request

        abstract fun limitEntityCount(): Int

        abstract fun currentList(): Collection<Content>

        abstract fun combineList(currentList: Collection<Content>, response: Res): Res

        abstract fun nextRequestFactory(offset: Int, latestRequest: Req?): Req?

        override val isBusy: AtomicBoolean = AtomicBoolean(false)

        override fun fetch(request: Req?) {
            if (!isTerminal() && isBusy.compareAndSet(false, true)) {
                latestRequestCache = requestCacheFactory(request)
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