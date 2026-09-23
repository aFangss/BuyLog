package com.buylog.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BuyLogDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
){
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_IMAGE_URL TEXT,
                $COLUMN_PRODUCT_URL TEXT,
                $COLUMN_PLATFORM TEXT,
                $COLUMN_PRICE TEXT,
                $COLUMN_IMAGES TEXT,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_SIZE TEXT,
                $COLUMN_ADDED_TIME INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 当前还没有数据库升级逻辑
    }

    companion object {
        private const val DATABASE_NAME = "buylog.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_PRODUCTS = "products"

        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_IMAGE_URL = "image_url"
        const val COLUMN_PRODUCT_URL = "product_url"
        const val COLUMN_PLATFORM = "platform"
        const val COLUMN_PRICE = "price"
        const val COLUMN_IMAGES = "images"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_SIZE = "size"
        const val COLUMN_ADDED_TIME = "added_time"
    }
}