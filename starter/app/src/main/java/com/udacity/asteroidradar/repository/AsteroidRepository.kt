package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.AsteroidApiService
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.asDataBaseModel
import com.udacity.asteroidradar.database.AsteroidsDataBase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception

class AsteroidRepository(private val database : AsteroidsDataBase) {
    
    val asteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroids()){
        it.asDomainModel()
    }
    
    suspend fun refreshAsteroids(){
        withContext(Dispatchers.IO){
            var asteroidListStringFromApi:String = ""
            try {
                asteroidListStringFromApi = AsteroidApi.retrofitService.getAsteroids()
                println("From API: " + asteroidListStringFromApi)
            }catch (e:Exception){
                println("Failure: ${e.message}")

            }


            val asteroidListFromApi = parseAsteroidsJsonResult(JSONObject(asteroidListStringFromApi)).toList()
            println("First element from API: "+asteroidListFromApi[1].codename)
            val asteroidFomApiDBFormat = asteroidListFromApi.asDataBaseModel()
            println("First element from API in DB format: "+asteroidFomApiDBFormat[1].codename)
            database.asteroidDao.insertAll(asteroidFomApiDBFormat)

            //println(database.asteroidDao.getAsteroids().value?.get(1))

        }
    }
    
}