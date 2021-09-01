package jp.co.arsaga.extensions.repository.common

/**
 * 結果 or エラー内容を保持する抽象クラス
 */
sealed class Result<out R> {

    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

/**
 * ResultがSuccess型かつ値を持っていればtrueを返す
 */
val Result<*>.succeeded: Boolean
    get() = this is Result.Success && data != null

fun <T> Result<T>.successOr(fallback: T): T {
    return (this as? Result.Success<T>)?.data ?: fallback
}
