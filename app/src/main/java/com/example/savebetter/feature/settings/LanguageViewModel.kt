package com.example.savebetter.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebetter.core.domain.usecase.i18n.GetLanguageUseCase
import com.example.savebetter.core.domain.usecase.i18n.SetLanguageUseCase
import com.example.savebetter.core.i18n.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing active application language selection and persistence.
 */
@HiltViewModel
class LanguageViewModel @Inject constructor(
    getLanguageUseCase: GetLanguageUseCase,
    private val setLanguageUseCase: SetLanguageUseCase
) : ViewModel() {

    val currentLanguage: StateFlow<AppLanguage> = getLanguageUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppLanguage.ENGLISH
        )

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            setLanguageUseCase(language)
        }
    }
}
