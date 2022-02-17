package com.serwylo.babyphone

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import com.serwylo.babyphone.databinding.ContactItemAddBinding

import com.serwylo.babyphone.databinding.ContactItemBinding

/**
 * [RecyclerView.Adapter] that can display a [Contact].
 *
 * @param canAdd If in a locked mode, don't show the option to add. We assume this is for play time
 *               rather than "setup the app" time.
 */
class ContactListViewAdapter(
    private val context: Context,
    private val contacts: List<Contact>,
    private val canAdd: Boolean,
    private val onTouch: (contact: Contact) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_CONTACT = 1
        const val VIEW_TYPE_ADD = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= contacts.size) {
            VIEW_TYPE_ADD
        } else {
            VIEW_TYPE_CONTACT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CONTACT ->
                ViewHolder(
                    ContactItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else ->
                ViewHolderAdd(
                    ContactItemAddBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_CONTACT -> {
                val h = holder as ViewHolder
                val contact = contacts[position]
                h.binding.avatar.setImageDrawable(AppCompatResources.getDrawable(context, contact.avatarDrawableId))
                h.binding.name.text = contact.label
                h.binding.root.setOnTouchListener { _, _ ->
                    onTouch(contact)
                    true
                }
            }
        }
    }

    override fun getItemCount(): Int = contacts.size + if (canAdd) 1 else 0

    inner class ViewHolder(val binding: ContactItemBinding) : RecyclerView.ViewHolder(binding.root)

    inner class ViewHolderAdd(binding: ContactItemAddBinding) : RecyclerView.ViewHolder(binding.root)

}