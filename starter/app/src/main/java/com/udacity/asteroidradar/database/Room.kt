package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao{

    @Query("select * from databaseasteroid")
    fun getAsteroids(): LiveData<List<DataBaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(asteroidsToInsert : List<DataBaseAsteroid>)

}

@Database(entities = [DataBaseAsteroid::class], version = 1)
abstract class AsteroidsDataBase: RoomDatabase(){
    abstract val asteroidDao:AsteroidDao
}

private lateinit var INSTANCE: AsteroidsDataBase

fun getDataBase(context: Context) : AsteroidsDataBase{
    synchronized(AsteroidsDataBase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                AsteroidsDataBase::class.java,
                "asteroids").build()
        }
    }
    return INSTANCE
}