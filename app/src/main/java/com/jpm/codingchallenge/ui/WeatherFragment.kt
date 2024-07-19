package com.jpm.codingchallenge.ui

import android.Manifest
import android.app.SearchManager
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.SearchView.OnSuggestionListener
import androidx.core.view.isVisible
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jpm.codingchallenge.R
import com.jpm.codingchallenge.data.LocalWeatherDataModel
import com.jpm.codingchallenge.databinding.FragmentWeatherBinding
import com.jpm.codingchallenge.viewmodel.MainViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeatherFragment: Fragment() {

    companion object {
        private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
    }

    private val viewModel by activityViewModels<MainViewModel>()

    private lateinit var binding: FragmentWeatherBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var cursorAdapter: SimpleCursorAdapter? = null

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission())
        { isGranted ->
            // Handle Permission granted/rejected
            if (isGranted) {
                try {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            location?.let {
                                viewModel.onLastKnownLocationRetrieved(location)
                            } ?: run {
                                showLocationPermissionNotGrantedDialog()
                            }
                        }
                        .addOnFailureListener {
                            showError(it.localizedMessage)
                        }
                } catch (e: SecurityException) {
                    showError(e.localizedMessage)
                }
            } else {
                showLocationPermissionNotGrantedDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSearchView()

        initObservers()

        activityResultLauncher.launch(LOCATION_PERMISSION)
    }

    private fun initSearchView() {
        val from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val to = intArrayOf(R.id.searchItemTextView)
        cursorAdapter = SimpleCursorAdapter(
            context,
            R.layout.item_city_search_suggestion,
            null,
            from,
            to,
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )

        with (binding.searchView) {
            suggestionsAdapter = cursorAdapter

            setOnQueryTextListener(object: OnQueryTextListener {

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.getCitySuggestions(newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
            })

            setOnSuggestionListener(object: OnSuggestionListener {

                override fun onSuggestionClick(position: Int): Boolean {
                    val cursor = binding.searchView.suggestionsAdapter.getItem(position) as Cursor
                    val index = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)
                    val selection = cursor.getString(index)
                    viewModel.onCitySuggestionClicked(selection)
                    return true
                }

                override fun onSuggestionSelect(position: Int): Boolean {
                    return true
                }
            })
        }
    }

    private fun initObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MainViewModel.UiState.Success -> {
                    populateCurrentWeather(state.weatherData)
                }
                is MainViewModel.UiState.PopulateCitySuggestions -> {
                    cursorAdapter?.changeCursor(state.cursor)
                }
                is MainViewModel.UiState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        state.errorMessage ?: getString(R.string.generic_error_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.progressFlag.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
        }
    }

    private fun populateCurrentWeather(weatherInfo: LocalWeatherDataModel) {
        val query = weatherInfo.countryName?.let {
            "${weatherInfo.cityName}, $it"
        } ?: run {
            weatherInfo.cityName
        }

        with (binding.searchView) {
            setQuery("", false)
            clearFocus()
        }

        binding.cityTextView.text = query

        binding.currentTemperatureView.text = weatherInfo.currentTemperature.let {
            getString(R.string.temperature_TEMPLATE, it)
        } ?: ""

        Picasso.get()
            .load(weatherInfo.weatherIconUrl)
            .into(binding.weatherIconView)

        binding.highLowTextView.text =
            getString(
                R.string.high_low_TEMPLATE,
                weatherInfo.maxTemperature,
                weatherInfo.minTemperature
            )

        binding.descriptionTextView.text =
            getString(R.string.description_TEMPLATE, weatherInfo.description, weatherInfo.feelsLikeTemperature)

        binding.currentConditionsTextView.text =
            getString(
                R.string.currentConditions_TEMPLATE,
                weatherInfo.windSpeed,
                weatherInfo.pressure,
                weatherInfo.humidity
            )

        binding.sunriseAndSunsetTextView.text =
            getString(
                R.string.sunrise_sunset_TEMPLATE,
                weatherInfo.sunriseTime,
                weatherInfo.sunsetTime
            )
    }

    private fun showLocationPermissionNotGrantedDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder
            .setTitle(R.string.location_permission_not_granted_dialog_title)
            .setMessage(R.string.location_permission_not_granted_dialog_message)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog?.dismiss()
                requireActivity().finish()
            }
            .setCancelable(false)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showError(errorMessage: String?) {
        Toast.makeText(
            requireContext(),
            errorMessage ?: getString(R.string.generic_error_message),
            Toast.LENGTH_LONG
        ).show()
    }
}