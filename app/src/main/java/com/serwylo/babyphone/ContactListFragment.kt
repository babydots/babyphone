package com.serwylo.babyphone

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * A fragment representing a list of [Contacts], similar to a normal "contact selector" page from
 * a phone dialer.
 */
class ContactListFragment : BottomSheetDialogFragment() {

    private var columnCount = 2
    private var canAdd = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
            canAdd = it.getBoolean(ARG_CAN_ADD)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.contact_item_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = ContactListViewAdapter(context, ContactManager.getContacts(context), canAdd) { selectedContact ->
                    contactSelectedListener?.invoke(selectedContact)
                }
            }
        }
        return view
    }

    private var contactSelectedListener: ((contact: Contact) -> Unit)? = null

    fun onContactSelected(listener: ((contact: Contact) -> Unit)?) {
        contactSelectedListener = listener
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_CAN_ADD = "can-add"

        @JvmStatic
        fun newInstance(columnCount: Int, canAdd: Boolean) =
            ContactListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    putBoolean(ARG_CAN_ADD, canAdd)
                }
            }
    }
}