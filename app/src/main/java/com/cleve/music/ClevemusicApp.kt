package com.cleve.music

import android.app.Application
import androidx.room.Room
import com.cleve.music.data.local.CleveDatabase
import com.cleve.music.data.remote.YouTubeExtractor
import com.cleve.music.data.repo.MusicRepository
import com.cleve.music.player.PlayerHolder

class ClevemusicApp : Application() {

    val database: CleveDatabase by lazy {
        Room.databaseBuilder(this, CleveDatabase::class.java, "clevemusic.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val extractor: YouTubeExtractor by lazy { YouTubeExtractor() }

    val repository: MusicRepository by lazy {
        MusicRepository(database, extractor)
    }

    val playerHolder: PlayerHolder by lazy { PlayerHolder(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ClevemusicApp
            private set

        fun from(context: android.content.Context): ClevemusicApp =
            context.applicationContext as ClevemusicApp
    }
}
