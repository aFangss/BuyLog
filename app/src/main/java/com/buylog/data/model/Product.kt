package com.buylog.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,        // 商品标题
    val imageUrl: String,     // 图片URL
    val productUrl: String,   // 商品链接
    val platform: String,     // 平台（淘宝/京东）
    val price: String = "",   // 价格
    val images: String = "",  // 轮播图，逗号分隔
    val addedTime: Long = System.currentTimeMillis(),  // 添加时间

    val category: String = "",
    val size: String = "",
)
