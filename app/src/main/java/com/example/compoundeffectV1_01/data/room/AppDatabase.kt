package com.example.compoundeffectV1_01.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.compoundeffectV1_01.data.room.appSystemInfo.AppSystemInfo
import com.example.compoundeffectV1_01.data.room.appSystemInfo.SystemDao
import com.example.compoundeffectV1_01.data.room.category.CategoryEntity
import com.example.compoundeffectV1_01.data.room.category.CategoryDao
import com.example.compoundeffectV1_01.data.room.event.Event
import com.example.compoundeffectV1_01.data.room.event.EventDao
import com.example.compoundeffectV1_01.data.room.typeConvertor.TypeConverter


@Database(entities = [CategoryEntity::class,Event::class, AppSystemInfo::class], version = 1, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class AppDatabase : RoomDatabase() {


    abstract fun categoryDao(): CategoryDao
    abstract fun eventDao(): EventDao
    abstract fun systemDao(): SystemDao




    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }

            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "compound_effect.db"
            )

            // Copy the pre-populated database file from assets
//            copyDatabaseFromAssets(context, "flight_search.db")

//            val localDateTimeConverter =LocalDateTimeConverter()
//            builder.addTypeConverter(localDateTimeConverter)
            return builder.build()
        }

//        private fun copyDatabaseFromAssets(context: Context, databaseName: String) {
//            val outputFile = context.getDatabasePath(databaseName)
//
//            if (!outputFile.exists()) {
//                try {
//                    val inputStream = context.assets.open("database/$databaseName")
//                    val outputStream = FileOutputStream(outputFile)
//                    val buffer = ByteArray(1024)
//                    var length: Int
//                    while (inputStream.read(buffer).also { length = it } > 0) {
//                        outputStream.write(buffer, 0, length)
//                    }
//                    outputStream.flush()
//                    outputStream.close()
//                    inputStream.close()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//        }
    }


}