package com.serwylo.babyphone.settingscontactlist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.serwylo.babyphone.databinding.SettingsContactListItemBinding
import com.serwylo.babyphone.db.entities.Contact
import com.squareup.picasso.Picasso

/**
 * [RecyclerView.Adapter] that can display a [Contact], allow it to be enabled/disabled, or deleted.
 */
class SettingsContactListViewAdapter(
    private val onEdit: (contact: Contact) -> Unit,
    private val onToggle: (contact: Contact, isEnabled: Boolean) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var contacts: List<Contact> = emptyList()

    fun setContacts(contacts: List<Contact>) {
        this.contacts = contacts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            SettingsContactListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val contact = contacts[position]

        (holder as ViewHolder).apply {

            if (contact.isDefault) {
                binding.caption.visibility = View.VISIBLE
                binding.root.setOnClickListener(null)
            } else {
                binding.caption.visibility = View.INVISIBLE
                binding.root.setOnClickListener { onEdit(contact) }
            }

            if (contact.avatarPath.isNotEmpty()) {
                Picasso.get().load(contact.avatarPath).fit().centerCrop().into(binding.avatar)
            }

            binding.name.text = contact.name

            binding.enabled.also { enabled ->
                enabled.setOnCheckedChangeListener(null)
                enabled.isChecked = contact.isEnabled
                enabled.setOnCheckedChangeListener { _, isChecked ->
                    onToggle(contact, isChecked)
                }
            }

        }
    }

    override fun getItemCount(): Int = contacts.size

    fun notifyContactRefresh(contact: Contact) {
        val index = contacts.indexOfFirst { it.id == contact.id }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    inner class ViewHolder(val binding: SettingsContactListItemBinding) : RecyclerView.ViewHolder(binding.root)

}