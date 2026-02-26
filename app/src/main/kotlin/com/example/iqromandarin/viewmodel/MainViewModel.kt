package com.example.iqromandarin.viewmodel

import androidx.lifecycle.*
import com.example.iqromandarin.database.AppDatabase
import com.example.iqromandarin.model.Halaman
import com.example.iqromandarin.model.Item
import com.example.iqromandarin.model.Jilid
import com.example.iqromandarin.srs.SRSEngine
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(private val db: AppDatabase) : ViewModel() {

    // ---- Jilid ----
    val allJilid: LiveData<List<Jilid>> = db.jilidDao().getAllJilid()
    val currentActiveJilid: LiveData<Jilid?> = db.jilidDao().getCurrentActiveJilid()

    fun getJilidById(jilidId: Int): LiveData<Jilid?> = db.jilidDao().getJilidById(jilidId)

    // ---- Halaman ----
    fun getHalamanByJilid(jilidId: Int): LiveData<List<Halaman>> =
        db.halamanDao().getHalamanByJilid(jilidId)

    // ---- Items ----
    fun getItemsByHalaman(halamanId: Int): LiveData<List<Item>> =
        db.itemDao().getItemsByHalaman(halamanId)

    // ---- Progress Stats ----
    val totalItemDikuasai: LiveData<Int> = db.itemDao().getTotalDikuasai()
    val totalHalamanSelesai: LiveData<Int> = db.halamanDao().getTotalHalamanSelesai()

    val currentJilidNumber: LiveData<Int> = currentActiveJilid.map { jilid ->
        jilid?.nomorJilid ?: 1
    }

    // ---- SRS Review ----
    fun getDueReviewItems(): LiveData<List<Item>> {
        val now = System.currentTimeMillis()
        return db.itemDao().getDueReviewItems(now)
    }

    val dailyReviewCount: LiveData<Int> = getDueReviewItems().map { it.size }

    // ---- Last unlocked halaman (for ViewPager position) ----
    fun getLastUnlockedHalaman(jilidId: Int): LiveData<Int> {
        return liveData {
            val page = db.halamanDao().getLastUnlockedPage(jilidId) ?: 0
            emit(page - 1) // Convert to 0-based index
        }
    }

    // ---- Update item mastery + SRS ----
    fun updateItemProgres(itemId: Int, isKuasai: Boolean, srsBox: Int, nextReviewDays: Int) {
        viewModelScope.launch {
            val nextReview = if (isKuasai) {
                SRSEngine.calculateNextReview(srsBox)
            } else {
                0L
            }
            db.itemDao().updateItemSRS(itemId, isKuasai, srsBox, nextReview)
            if (isKuasai) {
                db.progresDao().incrementItemDikuasai()
            }
        }
    }

    fun markItemWrong(itemId: Int) {
        viewModelScope.launch {
            db.itemDao().markItemWrong(itemId)
        }
    }

    // ---- Unlock next halaman after Lancar ----
    fun unlockNextHalaman(jilidId: Int, currentHalamanId: Int, currentIndex: Int) {
        viewModelScope.launch {
            val nextHalaman = db.halamanDao().getNextLockedHalaman(jilidId, currentIndex + 1)
            nextHalaman?.let {
                db.halamanDao().unlockHalaman(it.id)
            }

            // Check if all halaman in jilid are done → unlock next jilid
            val allHalaman = db.halamanDao().getHalamanCountByJilid(jilidId)
            val currentJilid = db.jilidDao().getJilidById(jilidId).value
            currentJilid?.let { jilid ->
                if (currentIndex + 1 >= allHalaman - 1) {
                    db.jilidDao().markJilidSelesai(jilidId)
                    // Unlock next jilid
                    db.jilidDao().unlockJilid(jilid.nomorJilid + 1)
                }
            }
        }
    }

    fun markHalamanSelesai(halamanId: Int) {
        viewModelScope.launch {
            db.halamanDao().markHalamanSelesai(halamanId)
            db.progresDao().incrementHalamanSelesai()
        }
    }

    // ---- Add custom item ----
    fun addCustomItem(item: Item) {
        viewModelScope.launch {
            db.itemDao().insertItem(item)
        }
    }
}

class MainViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
