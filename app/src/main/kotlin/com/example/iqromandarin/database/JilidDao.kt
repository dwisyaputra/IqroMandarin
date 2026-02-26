package com.example.iqromandarin.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.iqromandarin.model.Halaman
import com.example.iqromandarin.model.Jilid
import com.example.iqromandarin.model.Progres

@Dao
interface JilidDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJilid(jilid: Jilid): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllJilid(jilidList: List<Jilid>)

    @Update
    suspend fun updateJilid(jilid: Jilid)

    @Query("SELECT * FROM jilid ORDER BY nomorJilid ASC")
    fun getAllJilid(): LiveData<List<Jilid>>

    @Query("SELECT * FROM jilid WHERE id = :jilidId")
    fun getJilidById(jilidId: Int): LiveData<Jilid?>

    @Query("SELECT * FROM jilid WHERE isUnlocked = 1 ORDER BY nomorJilid DESC LIMIT 1")
    fun getCurrentActiveJilid(): LiveData<Jilid?>

    @Query("UPDATE jilid SET isUnlocked = 1 WHERE nomorJilid = :nomorJilid")
    suspend fun unlockJilid(nomorJilid: Int)

    @Query("UPDATE jilid SET isSelesai = 1 WHERE id = :jilidId")
    suspend fun markJilidSelesai(jilidId: Int)

    @Query("SELECT COUNT(*) FROM jilid")
    suspend fun getJilidCount(): Int
}

@Dao
interface HalamanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHalaman(halaman: Halaman): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHalaman(halamanList: List<Halaman>)

    @Update
    suspend fun updateHalaman(halaman: Halaman)

    @Query("SELECT * FROM halaman WHERE jilidId = :jilidId ORDER BY nomorHalaman ASC")
    fun getHalamanByJilid(jilidId: Int): LiveData<List<Halaman>>

    @Query("SELECT * FROM halaman WHERE id = :halamanId")
    suspend fun getHalamanById(halamanId: Int): Halaman?

    @Query("SELECT COUNT(*) FROM halaman WHERE isSelesai = 1")
    fun getTotalHalamanSelesai(): LiveData<Int>

    @Query("UPDATE halaman SET isUnlocked = 1 WHERE id = :halamanId")
    suspend fun unlockHalaman(halamanId: Int)

    @Query("UPDATE halaman SET isSelesai = 1 WHERE id = :halamanId")
    suspend fun markHalamanSelesai(halamanId: Int)

    // Get the index of last unlocked halaman in a jilid
    @Query("SELECT nomorHalaman FROM halaman WHERE jilidId = :jilidId AND isUnlocked = 1 ORDER BY nomorHalaman DESC LIMIT 1")
    suspend fun getLastUnlockedPage(jilidId: Int): Int?

    // Get next locked halaman
    @Query("SELECT * FROM halaman WHERE jilidId = :jilidId AND nomorHalaman > :currentPage AND isUnlocked = 0 ORDER BY nomorHalaman ASC LIMIT 1")
    suspend fun getNextLockedHalaman(jilidId: Int, currentPage: Int): Halaman?

    @Query("SELECT COUNT(*) FROM halaman WHERE jilidId = :jilidId")
    suspend fun getHalamanCountByJilid(jilidId: Int): Int
}

@Dao
interface ProgresDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgres(progres: Progres)

    @Update
    suspend fun updateProgres(progres: Progres)

    @Query("SELECT * FROM progres WHERE id = 1")
    fun getProgres(): LiveData<Progres?>

    @Query("SELECT * FROM progres WHERE id = 1")
    suspend fun getProgresDirect(): Progres?

    @Query("UPDATE progres SET totalItemDikuasai = totalItemDikuasai + 1 WHERE id = 1")
    suspend fun incrementItemDikuasai()

    @Query("UPDATE progres SET totalHalamanSelesai = totalHalamanSelesai + 1 WHERE id = 1")
    suspend fun incrementHalamanSelesai()

    @Query("UPDATE progres SET jilidAktif = :jilidId, halamanAktif = :halamanId WHERE id = 1")
    suspend fun updateAktif(jilidId: Int, halamanId: Int)
}
