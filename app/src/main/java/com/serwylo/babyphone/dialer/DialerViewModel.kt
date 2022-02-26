package com.serwylo.babyphone.dialer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.serwylo.babyphone.db.ContactDao
import com.serwylo.babyphone.db.entities.ContactWithSounds


class DialerViewModel(dao: ContactDao) : ViewModel() {

    val contact: LiveData<ContactWithSounds> = dao.getCurrentContact()

}

class DialerViewModelFactory(private val dao: ContactDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DialerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DialerViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
