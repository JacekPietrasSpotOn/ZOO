package com.jacekpietras.zoo.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan")
internal data class PlanDto(
    @PrimaryKey val planId: String,
    val stages: List<String>,
)
