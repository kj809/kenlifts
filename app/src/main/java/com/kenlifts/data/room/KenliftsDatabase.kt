package com.kenlifts.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RoutineEntity::class,
        ExerciseEntity::class,
        RoutineExerciseEntity::class,
        ExerciseWeightEntity::class,
        WorkoutEntity::class,
        WorkoutSetEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class KenliftsDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineExerciseDao(): RoutineExerciseDao
    abstract fun exerciseWeightDao(): ExerciseWeightDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutSetDao(): WorkoutSetDao

    companion object {
        private const val DATABASE_NAME = "kenlifts_db"

        fun create(context: Context): KenliftsDatabase = Room.databaseBuilder(
            context.applicationContext,
            KenliftsDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    DatabaseSeeder.seedSync(db)
                }
            })
            .build()
    }
}
