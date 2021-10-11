package com.udacity.asteroidradar.main

import android.app.Application
import android.transition.Visibility
import android.view.View
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.getDataBase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


enum class AsteroidApiStatus { LOADING, ERROR, DONE }

/**
 * Viewmodel attached to [MainViewModel]
 */
class MainViewModel(application: Application) : ViewModel() {

    private val _status = MutableLiveData<AsteroidApiStatus>()
    val status: LiveData<AsteroidApiStatus>
        get() = _status

    //Navigation variables
    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid>()
    val navigateToSelectedAsteroid: LiveData<Asteroid>
        get() = _navigateToSelectedAsteroid

    private val _asteroidList = MutableLiveData<List<Asteroid>>()
    val asteroidList: LiveData<List<Asteroid>>
        get() = _asteroidList

    //data Variables
    private val _response = MutableLiveData<ArrayList<Asteroid>>()
    val response: MutableLiveData<ArrayList<Asteroid>>
        get() = _response

    private val _isStatusBarVisible = MutableLiveData<Int>()
    val isStatusBarVisible: MutableLiveData<Int>
        get() = _isStatusBarVisible

    private val currentDateString: String

    private val database = getDataBase(application)
    private val asteroidRepository = AsteroidRepository(database)

    init {
        //Initial status
        _isStatusBarVisible.value = View.VISIBLE
        currentDateString = getCurrentDateTime().toString(Constants.DEVICE_DATE_FORMAT)

        //Loading data
        //getDataFromApi()
        updateDataOnDBAndLayout()
        println(_asteroidList.value?.get(0)?.codename)
    }

    //repository Methods
    fun updateDataOnDBAndLayout(){
        viewModelScope.launch {
            asteroidRepository.refreshAsteroids()
            _asteroidList.value = asteroidRepository.asteroids.value
        }

    }


    //date methods
    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    private fun getFakeData() {
        val asteroideUno =
            Asteroid(1, "Prueba de nombre", "Fecha aqui", 34.3, 34.4, 34.3, 34.5, true)
        val asteroideTwo = Asteroid(2, "Nombresillop", "Fecha 2", 34.5, 32.4, 74.3, 74.5, false)
        _asteroidList.value = listOf(asteroideTwo, asteroideUno)
    }


    //Update methods
    fun getDataFromApi() {

        viewModelScope.launch {
            _status.value = AsteroidApiStatus.LOADING
            try {
                val rowListResult =
                    AsteroidApi.retrofitService.getAsteroids(currentDateString, Constants.DEMOKEY)
                _asteroidList.value = parseAsteroidsJsonResult(JSONObject(rowListResult)).toList()
                _isStatusBarVisible.value = View.GONE
                _status.value = AsteroidApiStatus.DONE
            } catch (e: Exception) {
                val rowResult = "Failure: ${e.message}"
                _status.value = AsteroidApiStatus.ERROR
                _asteroidList.value = listOf()
            }
        }

    }

    //Navigation Methods
    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedAsteroid.value = null
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}