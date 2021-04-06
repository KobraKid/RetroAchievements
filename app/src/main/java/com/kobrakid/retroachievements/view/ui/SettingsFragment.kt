package com.kobrakid.retroachievements.view.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kobrakid.retroachievements.Consts
import com.kobrakid.retroachievements.R
import com.kobrakid.retroachievements.databinding.FragmentSettingsBinding
import com.kobrakid.retroachievements.viewmodel.SettingsViewModel

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        activity?.title = "Settings"
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsThemeDropdown.apply {
            try {
                adapter = object : ArrayAdapter<Consts.Theme>(requireContext(), android.R.layout.simple_spinner_dropdown_item, Consts.Theme.values()) {
                    override fun isEnabled(position: Int): Boolean {
                        return Consts.Theme.values()[position].enabled
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val textView = super.getDropDownView(position, convertView, parent) as TextView
                        if (isEnabled(position)) {
                            val typedValue = TypedValue()
                            context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                textView.setTextColor(context.resources.getColor(typedValue.resourceId, activity?.theme))
                            else
                                textView.setTextColor(ContextCompat.getColor(context, typedValue.resourceId))
                        } else {
                            textView.setTextColor(Color.GRAY)
                        }
                        return textView
                    }
                }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Context was null", e)
                return // no need to continue if this fragment is not attached to a context
            }
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
                override fun onItemSelected(adapterView: AdapterView<*>, view: View?, pos: Int, id: Long) {
                    if (isEnabled) viewModel.setTheme(pos)
                }
            }
        }
        viewModel.settings.observe(viewLifecycleOwner) {
            val themeIndex = Consts.Theme.values()
                    .indexOfFirst { theme -> theme.themeAttr == it.theme }
                    .coerceAtLeast(0)
            binding.settingsCurrentTheme.text = getString(R.string.settings_current_theme, if (themeIndex == 0) "<none>" else Consts.Theme.values()[themeIndex].themeName)
            binding.settingsCurrentUser.text = if (it.user == "") getString(R.string.settings_no_current_user) else getString(R.string.settings_current_user, it.user)
            binding.settingsThemeDropdown.setSelection(themeIndex)
            binding.settingsHideConsoles.isChecked = it.hideEmptyConsoles
            binding.settingsHideGames.isChecked = it.hideEmptyGames
            binding.settingsHideConsolesWarning.visibility = if (it.hideEmptyConsoles) View.GONE else View.VISIBLE
            binding.settingsHideGamesWarning.visibility = if (it.hideEmptyGames) View.GONE else View.VISIBLE
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.settingsApplyingFade.visibility = if (it) View.VISIBLE else View.GONE
            binding.settingsApplying.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.activityNeedsRecreating.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.onRecreate()
                activity?.recreate()
            }
        }
        binding.settingsHideConsoles.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean -> viewModel.setHideConsoles(context, b) }
        binding.settingsHideGames.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean -> viewModel.setHideGames(context, b) }
        binding.settingsLogoutButton.setOnClickListener { viewModel.logout(context) }
        binding.settingsApply.setOnClickListener { viewModel.applyTheme(context) }
        viewModel.init(context)
    }

    companion object {
        private val TAG = Consts.BASE_TAG + SettingsFragment::class.java.simpleName
    }
}