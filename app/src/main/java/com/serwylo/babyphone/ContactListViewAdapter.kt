package com.serwylo.babyphone

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources

import com.serwylo.babyphone.databinding.ContactItemBinding

/**
 * [RecyclerView.Adapter] that can display a [Contact].
 */
class ContactListViewAdapter(
    private val context: Context,
    private val contacts: List<Contact>,
    private val onTouch: (contact: Contact) -> Unit,
) : RecyclerView.Adapter<ContactListViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ContactItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.binding.avatar.setImageDrawable(AppCompatResources.getDrawable(context, contact.avatarDrawableId))
        holder.binding.name.text = contact.label
        holder.binding.root.setOnTouchListener { _, _ ->
            onTouch(contact)
            true
        }
    }

    override fun getItemCount(): Int = contacts.size

    inner class ViewHolder(val binding: ContactItemBinding) : RecyclerView.ViewHolder(binding.root)

}