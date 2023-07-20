package com.serwylo.babyphone.contactlist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.serwylo.babyphone.databinding.ContactItemBinding
import com.serwylo.babyphone.db.entities.Contact
import com.squareup.picasso.Picasso

/**
 * [RecyclerView.Adapter] that can display a [Contact].
 */
class ContactListViewAdapter(
    private val onTouch: (contact: Contact) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var contacts: List<Contact> = emptyList()

    fun setContacts(contacts: List<Contact>) {
        this.contacts = contacts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            ContactItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val h = holder as ViewHolder
        val contact = contacts[position]

        if (contact.avatarPath.isNotEmpty()) {
            Picasso.get().load(contact.avatarPath).fit().centerCrop().into(h.binding.avatar)
        }

        h.binding.name.text = contact.name
        h.binding.root.setOnTouchListener { _, _ ->
            onTouch(contact)
            true
        }
    }

    override fun getItemCount(): Int = contacts.size

    inner class ViewHolder(val binding: ContactItemBinding) : RecyclerView.ViewHolder(binding.root)

}