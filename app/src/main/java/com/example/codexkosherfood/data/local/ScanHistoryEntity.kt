package com.example.codexkosherfood.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val verdict: String,
    val recognizedText: String,
    val editedText: String,
    val parsedSection: String,
    val resultJson: String,
    val aiReviewJson: String,
)
