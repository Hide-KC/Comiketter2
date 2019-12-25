package com.kc.comiketter2.model.data

import android.content.Context
import com.kc.comiketter2.model.data.room.source.AppLocalDataSource
import com.kc.comiketter2.model.data.room.source.AppLocalDatabase

object Injection {
  fun provideTasksRepository(context: Context): AppRepository {
    val localDatabase = AppLocalDatabase.getInstance(context)
    return AppRepository.getInstance(
      AppLocalDataSource.getInstance(localDatabase.appDao())
    )
  }
}