package com.serwylo.babyphone.settingscontactlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.serwylo.babyphone.db.ContactDao
import com.serwylo.babyphone.db.entities.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsContactListViewModel(private val dao: ContactDao) : ViewModel() {

    val contacts: LiveData<List<Contact>> = dao.loadAllContacts()

    /**
     * If there is only one contact left enabled and we ask to disaable it, return false.
     */
    fun toggleContact(contact: Contact, enabled: Boolean): Boolean {
        val numCurrentlyEnabled = contacts.value?.count { it.isEnabled } ?: 0
        return if (enabled || numCurrentlyEnabled > 1) {
            viewModelScope.launch(Dispatchers.IO) {
                dao.update(contact.copy(isEnabled = enabled))

                if (!enabled && dao.getCurrentContactId() == contact.id) {
                    dao.pickNewCurrentContact()
                }
            }
            true
        } else {
            false
        }

    }
}

class SettingsContactListViewModelFactory(private val dao: ContactDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsContactListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsContactListViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
