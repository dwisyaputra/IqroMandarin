package com.example.iqromandarin.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.iqromandarin.model.Item

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Item>)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("SELECT * FROM items WHERE halamanId = :halamanId ORDER BY id ASC")
    fun getItemsByHalaman(halamanId: Int): LiveData<List<Item>>

    @Query("SELECT * FROM items WHERE jilidId = :jilidId ORDER BY id ASC")
    fun getItemsByJilid(jilidId: Int): LiveData<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemById(id: Int): LiveData<Item?>

    @Query("SELECT * FROM items WHERE isKuasai = 0 AND jilidId <= :maxJilid ORDER BY RANDOM() LIMIT :limit")
    fun getItemsForReview(maxJilid: Int, limit: Int = 20): LiveData<List<Item>>

    // SRS: items due for review today
    @Query("SELECT * FROM items WHERE nextReviewAt > 0 AND nextReviewAt <= :nowMillis ORDER BY nextReviewAt ASC")
    fun getDueReviewItems(nowMillis: Long): LiveData<List<Item>>

    @Query("SELECT COUNT(*) FROM items WHERE isKuasai = 1")
    fun getTotalDikuasai(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM items WHERE halamanId = :halamanId")
    suspend fun getItemCountByHalaman(halamanId: Int): Int

    @Query("UPDATE items SET isKuasai = :isKuasai, srsBox = :srsBox, nextReviewAt = :nextReviewAt, reviewCount = reviewCount + 1 WHERE id = :itemId")
    suspend fun updateItemSRS(itemId: Int, isKuasai: Boolean, srsBox: Int, nextReviewAt: Long)

    @Query("UPDATE items SET wrongCount = wrongCount + 1, srsBox = 0 WHERE id = :itemId")
    suspend fun markItemWrong(itemId: Int)

    // All items count
    @Query("SELECT COUNT(*) FROM items")
    suspend fun getTotalItemCount(): Int

    // Custom items
    @Query("SELECT * FROM items WHERE isCustom = 1 ORDER BY id DESC")
    fun getCustomItems(): LiveData<List<Item>>
}
