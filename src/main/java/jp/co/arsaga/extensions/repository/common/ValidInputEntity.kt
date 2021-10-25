package jp.co.arsaga.extensions.repository.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KMutableProperty0

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
) : ValidInputEntityInterface<T> {
    override val inputValueReference: () -> T = { inputValues }

    /**
     * JetpackCompose内からviewModelやuseCase内の状態を変更する関数
     * IO待ちなどの重い処理は別スレッドでするようにしてください
     *
     * @param updater 値を変換する
     */
    override fun update(updater: T.() -> T) {
        updater(inputValues).let { newValue ->
            copy(
                inputValues = newValue,
                isButtonEnabled = validator(newValue)
            ).also {
                sideEffect(it)
            }
        }
    }

    data class Factory<T> constructor(
        private val factory: ((ValidInputEntity<T>) -> Unit) -> ValidInputEntity<T>
    ) {
        private val _state = MutableStateFlow(factory(::updater))
        val state: StateFlow<ValidInputEntityInterface<T>> = _state

        private fun updater(validInputEntity: ValidInputEntity<T>) {
            _state.value = validInputEntity
        }

        companion object {
            @Deprecated(
                "Factoryのconstructorで完結させたい(リファクタ予定)"
            )
            fun <T> create(
                inputValues: T,
                validator: (T) -> Boolean
            ): Factory<T> = Factory {
                ValidInputEntity(inputValues, it) { entity ->
                    validator(entity)
                }
            }
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
data class ComplexValidInputEntity<T> constructor(
    override val inputValueReference: () -> T,
    val sideEffect: (T, ComplexValidInputEntity<T>) -> Unit,
    private val updateNotifier: Boolean = true,
    override val isButtonEnabled: Boolean = false,
    override val validator: (T) -> Boolean
) : ValidInputEntityInterface<T> {
    override fun update(updater: T.() -> T) {
        inputValueReference()
            .let(updater).let { newValue ->
                copy(
                    updateNotifier = !updateNotifier,
                    isButtonEnabled = validator(newValue)
                ).also {
                    sideEffect(newValue, it)
                }
            }
    }

    data class Factory<T> constructor(
        private val reflection: KMutableProperty0<T>,
        private val factory: ((T, ComplexValidInputEntity<T>) -> Unit) -> ComplexValidInputEntity<T>
    ) {
        private val _state = MutableStateFlow(factory(::updater))
        val state: StateFlow<ValidInputEntityInterface<T>> = _state

        private fun updater(newValue: T, complexValidInputEntity: ComplexValidInputEntity<T>) {
            reflection.set(newValue)
            _state.value = complexValidInputEntity
        }

        companion object {
            @Deprecated(
                "Factoryのconstructorで完結させたい(リファクタ予定)"
            )
            fun <T> create(
                reflection: KMutableProperty0<T>,
                validator: (T) -> Boolean
            ): Factory<T> = Factory(reflection) {
                ComplexValidInputEntity({ reflection.get() }, it) { entity ->
                    validator(entity)
                }
            }
        }
    }
}

/**
 * @constructor isButtonEnabled バリデーションの結果ボタンを有効/無効の値
 * @constructor validator newValueに基づいてisButtonEnabledを判定するロジック
 */
sealed interface ValidInputEntityInterface<T> : InputEntityInterface<T> {
    val isButtonEnabled: Boolean
    val validator: (T) -> Boolean
}

data class InputEntity<T> internal constructor(
    private val inputValues: T,
    private val sideEffect: (InputEntity<T>) -> Unit = {}
) : InputEntityInterface<T> {
    override val inputValueReference: () -> T = { inputValues }
    override fun update(updater: T.() -> T) {
        updater(inputValues).let { newValue ->
            copy(
                inputValues = newValue
            ).also {
                sideEffect(it)
            }
        }
    }

    data class Factory<T>(private val initValue: T) {
        private val _state = MutableStateFlow(InputEntity(initValue, (::updater)))
        val state: StateFlow<InputEntityInterface<T>> = _state

        private fun updater(inputEntity: InputEntity<T>) {
            _state.value = inputEntity
        }
    }
}

interface InputEntityInterface<T> {
    val inputValueReference: () -> T
    fun update(updater: T.() -> T)
}