package com.serwylo.babyphone.contactlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.serwylo.babyphone.db.ContactDao
import com.serwylo.babyphone.db.entities.Contact


class ContactListViewModel(dao: ContactDao) : ViewModel() {

    val contacts: LiveData<List<Contact>> = dao.loadEnabledContacts()

}

class ContactListViewModelFactory(private val dao: ContactDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactListViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
