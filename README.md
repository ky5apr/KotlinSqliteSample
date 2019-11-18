# KotlinSqliteSample
This program describes how to operate Sqlite on Android with Kotlin.

## CREATE TABLE & ALTER TABLE

```
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

    private class SampleDBHelper(context: Context, databaseName:String,
                                 factory: SQLiteDatabase.CursorFactory?, version: Int) :
        SQLiteOpenHelper(context, databaseName, factory, version) {

        override fun onCreate(database: SQLiteDatabase?) {
            database?.execSQL("create table if not exists SampleTable (id text primary key, name text, type integer, image BLOB)");
        }

        override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            if (oldVersion < newVersion) {
                database?.execSQL("alter table SampleTable add column deleteFlag integer default 0")
            }
        }
    }
```

## INSERT

```
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private val dbName: String = "SampleDB"
    private val tableName: String = "SampleTable"
    private val dbVersion: Int = 1

    private fun insertData(id: String, name: String, type: Int, bitmap: Bitmap) {
        try {
            val dbHelper = SampleDBHelper(applicationContext, dbName, null, dbVersion);
            val database = dbHelper.writableDatabase

            val values = ContentValues()
            values.put("id", id)
            values.put("name", name)
            values.put("type", type)
            val byteArrayOutputStream = ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val bytes = byteArrayOutputStream.toByteArray()
            values.put("image", bytes)

            database.insertOrThrow(tableName, null, values)
        }catch(exception: Exception) {
            Log.e("insertData", exception.toString())
        }
    }
}
```

## UPDATE

```
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private val dbName: String = "SampleDB"
    private val tableName: String = "SampleTable"
    private val dbVersion: Int = 1

    private fun updateData(whereId: String, newName: String, newType: Int, newBitmap: Bitmap) {
        try {
            val dbHelper = SampleDBHelper(applicationContext, dbName, null, dbVersion);
            val database = dbHelper.writableDatabase

            val values = ContentValues()
            values.put("name", newName)
            values.put("type", newType)
            val byteArrayOutputStream = ByteArrayOutputStream();
            newBitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val bytes = byteArrayOutputStream.toByteArray()
            values.put("image", bytes)

            val whereClauses = "id = ?"
            val whereArgs = arrayOf(whereId)
            database.update(tableName, values, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("updateData", exception.toString())
        }
    }
}
```

## DELETE

```
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    private val dbName: String = "SampleDB"
    private val tableName: String = "SampleTable"
    private val dbVersion: Int = 1

    private fun deleteData(whereId: String) {
        try {
            val dbHelper = SampleDBHelper(applicationContext, dbName, null, dbVersion);
            val database = dbHelper.writableDatabase

            val whereClauses = "id = ?"
            val whereArgs = arrayOf(whereId)
            database.delete(tableName, whereClauses, whereArgs)
        }catch(exception: Exception) {
            Log.e("deleteData", exception.toString())
        }
    }
}
```

## SELECT

```
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private val dbName: String = "SampleDB"
    private val tableName: String = "SampleTable"
    private val dbVersion: Int = 1
    private var arrayListId: ArrayList<String> = arrayListOf()
    private var arrayListName: ArrayList<String> = arrayListOf()
    private var arrayListType: ArrayList<Int> = arrayListOf()
    private var arrayListBitmap: ArrayList<Bitmap> = arrayListOf()

    private fun selectData() {
        try {
            arrayListId.clear();arrayListName.clear();arrayListType.clear();arrayListBitmap.clear()

            val dbHelper = SampleDBHelper(applicationContext, dbName, null, dbVersion)
            val database = dbHelper.readableDatabase

            val sql = "select id, name, type, image from " + tableName + " where type in (1, 2) order by id"

            val cursor = database.rawQuery(sql, null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    arrayListId.add(cursor.getString(0))
                    arrayListName.add(cursor.getString(1))
                    arrayListType.add(cursor.getInt(2))
                    val blob: ByteArray = cursor.getBlob(3)
                    val bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.size)
                    arrayListBitmap.add(bitmap)
                    cursor.moveToNext()
                }
            }
        }catch(exception: Exception) {
            Log.e("selectData", exception.toString());
        }
    }
}
```
