package jp.co.arsaga.extensions.repository.common

/**
 * JetpackComposeにおいてフォームバリデーションを抽象化した型クラス
 */

/**
 * @constructor inputValues
 * ボタンのバリデーション対象になる型クラスの初期値を置く
 * 基本はAPIのリクエスト型を置くがそれで表現仕切れない場合はMapやPairやDTOなどを挟んで表現の幅を広げる
 *
 * @constructor sideEffect
 * ValidInputEntityを保持しているプロパティを更新する(もしくは更新不可でガードする)関数への参照が基本だが、
 * 値が更新されるついでに何か(リアクションやカウントアップ等)する処理を置く場所
 */
data class ValidInputEntity<T>(
    val inputValues: T,
    val sideEffect: (ValidInputEntity<T>) -> Unit = {},
    override val isButtonEnabled: Boolean = false,
    override val validator: (T) -> Boolean
) : AbstractValidInputEntity<T> {
    /**
     * JetpackCompose内からviewModelやuseCase内の状態を変更する関数
     * IO待ちなどの重い処理は別スレッドでするようにしてください
     *
     * @param updater 値を変換する
     */
    fun update(updater: T.() -> T) = updater(inputValues).let { newValue ->
        copy(
            inputValues = newValue,
            isButtonEnabled = validator(newValue)
        ).also {
            sideEffect(it)
        }
    }
}

/**
 * 複数画面に跨いで入力フォームがあるリクエストパラメーターに対応するための型
 *
 * @constructor inputValueReference リクエスト型を置いている別のプロパティの値を取得する関数
 *
 * @constructor sideEffect リクエスト型を置いている別のプロパティを更新する関数
 */
data class ComplexValidInputEntity<T>(
    val inputValueReference: () -> T,
    val sideEffect: (T, ComplexValidInputEntity<T>) -> Unit,
    private val updateNotifier: Boolean = true,
    override val isButtonEnabled: Boolean = false,
    override val validator: (T) -> Boolean
) : AbstractValidInputEntity<T> {
    fun update(updater: T.() -> T) = inputValueReference()
        .let(updater).let { newValue ->
            copy(
                updateNotifier = !updateNotifier,
                isButtonEnabled = validator(newValue)
            ).also {
                sideEffect(newValue, it)
            }
        }
}

/**
 * @constructor isButtonEnabled バリデーションの結果ボタンを有効/無効の値
 * @constructor validator newValueに基づいてisButtonEnabledを判定するロジック
 */
sealed interface AbstractValidInputEntity<T> {
    val isButtonEnabled: Boolean
    val validator: (T) -> Boolean
}