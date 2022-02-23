package com.serwylo.babyphone.contactlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.serwylo.babyphone.databinding.ContactItemListBinding
import com.serwylo.babyphone.db.AppDatabase
import com.serwylo.babyphone.db.entities.Contact
import com.serwylo.babyphone.editcontact.EditContactActivity

/**
 * A fragment representing a list of [Contact]s, similar to a normal "contact selector" page from
 * a phone dialer.
 */
class ContactListFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: ContactListViewModel
    private var columnCount = 3
    private var canAdd = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ContactListViewModelFactory(AppDatabase.getInstance(requireContext()).contactDao())
        ).get(ContactListViewModel::class.java)

        arguments?.let {
            canAdd = it.getBoolean(ARG_CAN_ADD)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = ContactItemListBinding.inflate(inflater, container, false)

        binding.list.layoutManager = GridLayoutManager(context, columnCount)

        binding.list.adapter = ContactListViewAdapter(
            { selectedContact -> contactSelectedListener?.invoke(selectedContact) },
            { startActivity(Intent(context, EditContactActivity::class.java)) },
        ).also { adapter ->

            viewModel.contacts.observe(this) { contacts ->
                adapter.setContacts(contacts)
            }

        }

        return binding.root

    }

    private var contactSelectedListener: ((contact: Contact) -> Unit)? = null

    fun onContactSelected(listener: ((contact: Contact) -> Unit)?) {
        contactSelectedListener = listener
    }

    companion object {

        const val ARG_CAN_ADD = "can-add"

        @JvmStatic
        fun newInstance(canAdd: Boolean) =
            ContactListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_CAN_ADD, canAdd)
                }
            }
    }
}