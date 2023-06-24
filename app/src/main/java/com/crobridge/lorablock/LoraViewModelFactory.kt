
package com.crobridge.lorablock

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crobridge.lorablock.db.LoraDao
import com.crobridge.lorablock.LoraViewModel

class ViewModelFactory(
    private val dataSource: LoraDao,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoraViewModel::class.java)) {
            return LoraViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

