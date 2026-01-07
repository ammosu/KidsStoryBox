package com.example.kidsstory.data.database.dao

import androidx.room.*
import com.example.kidsstory.data.database.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

/**
 * 角色資料存取物件
 */
@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :characterId")
    suspend fun getCharacterById(characterId: String): CharacterEntity?

    @Query("SELECT * FROM characters WHERE role = :role")
    suspend fun getCharactersByRole(role: String): List<CharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)
}
