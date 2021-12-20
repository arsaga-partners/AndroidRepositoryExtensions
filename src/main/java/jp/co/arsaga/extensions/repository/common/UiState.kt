package jp.co.arsaga.extensions.repository.common

/**
 * IOから取得する処理で、データとは別にをエラー状況と読み込み中の状況を個別に管理できるクラス
 * 画面に最終結果・読み込み中・エラーのそれぞれの状態を表示するのに便利
 * 主にGET系の処理でRepositoryとセットで使う
 */
data class UiState<T>(
    val data: T? = null,
    val loading: Boolean = false,
    val exception: Exception? = null,
) {
    /**
     * エラーかどうか
     */
    val hasError: Boolean
        get() = exception != null

    /**
     * 初回読み込み中かどうか
     */
    val initialLoad: Boolean
        get() = data == null && loading && !hasError

    fun <N>convert(newTypeData: N): UiState<N> = UiState(
        data = newTypeData,
        loading = loading,
        exception = exception
    )
}

/**
 * IOの成功/失敗時の値渡しの処理を抽象化したもの
 */
fun <T> UiState<T>.copyWithResult(
    value: Result<T>
): UiState<T> = when (value) {
    is Result.Success -> copy(loading = false, exception = null, data = value.data)
    is Result.Error -> copy(loading = false, exception = value.exception)
}
