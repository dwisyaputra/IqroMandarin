package com.example.iqromandarin.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.iqromandarin.model.Halaman
import com.example.iqromandarin.model.Item
import com.example.iqromandarin.model.Jilid
import com.example.iqromandarin.model.Progres
import com.example.iqromandarin.util.DataInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Item::class, Jilid::class, Halaman::class, Progres::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun jilidDao(): JilidDao
    abstract fun halamanDao(): HalamanDao
    abstract fun progresDao(): ProgresDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iqro_mandarin_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Populate database with initial data on first creation
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    DataInitializer.populateDatabase(context, database)
                }
            }
        }
    }
}
