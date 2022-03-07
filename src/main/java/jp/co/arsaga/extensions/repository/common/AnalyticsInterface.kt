package jp.co.arsaga.extensions.repository.common

import android.content.Context
import androidx.annotation.StringRes
import kotlinx.coroutines.flow.Flow

@JvmInline
value class AnalyticsStringResource(
    @StringRes val resourceId: Int
)

interface BaseAnalyticsDispatcher<T : Enum<T>> {
    fun post(
        context: Context,
        analyticsDataFactory: BaseAnalyticsDataFactory<T>,
    )
}

interface BaseAnalyticsDataFactory<AnalyticsEventType : Enum<AnalyticsEventType>> {
    val analyticsEventType: AnalyticsEventType
    fun create(): MutableMap<String, Any>?
}

interface BaseAnalyticsFragment<T : Enum<T>> {
    val analyticsDataFactory: Flow<BaseAnalyticsDataFactory<T>?>
}

interface BaseAnalyticsAction<T : Enum<T>> {
    val analyticsAction: (List<BaseAnalyticsDataFactory<T>>) -> Unit
}