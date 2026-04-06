package com.rohitkhandelwal.tickr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rohitkhandelwal.tickr.data.local.entity.SyncStateEntity
import com.rohitkhandelwal.tickr.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query(
        """
        SELECT * FROM tasks
        WHERE isDeleted = 0
        ORDER BY updatedAt DESC
        """
    )
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE isDeleted = 0 AND isCompleted = 0
        ORDER BY updatedAt DESC
        """
    )
    fun observeActiveTasks(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE isDeleted = 0 AND isCompleted = 1
        ORDER BY updatedAt DESC
        """
    )
    fun observeCompletedTasks(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE id = :taskId
        LIMIT 1
        """
    )
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query(
        """
        DELETE FROM tasks
        WHERE id = :taskId
        """
    )
    suspend fun deleteTaskById(taskId: String)

    @Query(
        """
        SELECT * FROM tasks
        WHERE syncState != :syncedState AND syncState != :syncingState
        ORDER BY updatedAt ASC
        """
    )
    suspend fun getPendingSyncTasks(
        syncedState: SyncStateEntity = SyncStateEntity.SYNCED,
        syncingState: SyncStateEntity = SyncStateEntity.SYNCING
    ): List<TaskEntity>
}
