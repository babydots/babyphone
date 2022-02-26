package com.serwylo.babyphone.settingscontactlist

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.serwylo.babyphone.R
import com.serwylo.babyphone.SettingsActivity
import com.serwylo.babyphone.ThemeManager
import com.serwylo.babyphone.databinding.ActivitySettingsContactListBinding
import com.serwylo.babyphone.db.AppDatabase
import com.serwylo.babyphone.db.entities.Contact
import com.serwylo.babyphone.editcontact.EditContactActivity

class SettingsContactListActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingsContactListViewModel

    private lateinit var adapter: SettingsContactListViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        // Not sure why, but this has to come before super.onCreate(), or else the light theme
        // will have a dark background, making the text very hard to read. This seems to fix it,
        // though I have not investigated why. Source: https://stackoverflow.com/a/15657428.
        ThemeManager.applyTheme(this)

        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            SettingsContactListViewModelFactory(AppDatabase.getInstance(this).contactDao())
        ).get(SettingsContactListViewModel::class.java)

        val binding = ActivitySettingsContactListBinding.inflate(layoutInflater)

        binding.list.also { list ->

            list.layoutManager = LinearLayoutManager(this)
            list.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
            list.adapter = SettingsContactListViewAdapter(
                onEdit = { contact -> onEditContact(contact) },
                onToggle = { contact, isEnabled -> onToggleContact(contact, isEnabled) },
            ).also { adapter ->

                this.adapter = adapter

                viewModel.contacts.observe(this) { contacts ->
                    adapter.setContacts(contacts)
                }

            }

        }

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { NavUtils.navigateUpFromSameTask(this) }

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_contact_list_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                startActivity(Intent(this, EditContactActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onToggleContact(contact: Contact, enabled: Boolean) {
        if (!viewModel.toggleContact(contact, enabled)) {
            Toast.makeText(this, "Your phone needs at least one contact enabled.", Toast.LENGTH_SHORT).show()
            adapter.notifyContactRefresh(contact)
        }
    }

    private fun onEditContact(contact: Contact) {
        startActivity(
            Intent(this, EditContactActivity::class.java).apply {
                putExtra(EditContactActivity.CONTACT_ID, contact.id)
            }
        )
    }

}