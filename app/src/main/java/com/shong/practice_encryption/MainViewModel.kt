package com.shong.practice_encryption

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.shong.practice_encryption.db.AppDatabase
import com.shong.practice_encryption.db.entity.TokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//ViewModel은 Lifecycle 또는 view, activity context를 참조하면 메모리 릭이 발생하므로, 이러한 객체를 참조해서는 안됩니다.
//또한 LiveData와 같은 LifecycleObserver들을 포함할 수 있지만, ViewModel은 (LiveData와 같은) lifecycler-aware observable들의 변화를 observe해서는 안됩니다.
class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository: Repository = Repository(AppDatabase.getDatabase(application, viewModelScope))
    var allUsers: LiveData<List<TokenEntity>> = repository.tokens

    fun insert(tokenEntity: TokenEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(tokenEntity)
    }

}