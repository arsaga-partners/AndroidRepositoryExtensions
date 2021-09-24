package jp.co.arsaga.extensions.repository.common

/**
 * UiStateで画面状態をハンドリングする際、Viewの受け取る引数が
 * (uiStateA, uiStateB..., sendActionA, sendActionB...)となるのを
 * (iOActionEntityA, iOActionEntityB...)とすることで
 * 1.引数の数を半分に削減
 * 2.UiStateとsendActionの対応関係を型の中に閉じ込める
 * の2点で複雑さの削減をするクラス
 */

/**
 * @constructor result IOの返り値が入るプロパティ
 * @constructor send IO処理を実行するラムダ
 */
data class IOActionEntity<T>(val result: UiState<T>?, val send: () -> Unit)

/**
 * 1つのAPIに対して複数のボタンから複数の叩き方をする想定のクラス
 *
 * @constructor send 引数にEnumを受け取るメソッド
 */
data class IOComplexActionEntity<T, K : Enum<K>>(val result: UiState<T>?, val send: (K) -> Unit)