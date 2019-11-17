package net.studioks.kotlinsqlitesample

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {
    private val dbName: String = "SampleDB"
    private val tableName: String = "SampleTable"
    private val dbVersion: Int = 1
    private var arrayListId: ArrayList<String> = arrayListOf()
    private var arrayListName: ArrayList<String> = arrayListOf()
    private var arrayListType: ArrayList<Int> = arrayListOf()
    private var arrayListBitmap: ArrayList<Bitmap> = arrayListOf()

    private lateinit var editId: EditText
    private lateinit var editName: EditText
    private lateinit var radioType1: RadioButton
    private lateinit var radioType2: RadioButton
    private lateinit var imageView: ImageView
    private lateinit var buttonPicture: Button
    private lateinit var buttonSelect: Button
    private lateinit var buttonInsert: Button
    private lateinit var buttonUpdate: Button
    private lateinit var buttonDelete: Button
    private lateinit var listView: ListView
    private lateinit var sampleDBAdapter: SampleDBAdapter

    private val requestCodeForPicture = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editId = findViewById(R.id.editId)
        editName = findViewById(R.id.editName)
        radioType1 = findViewById(R.id.radioType1)
        radioType2 = findViewById(R.id.radioType2)
        imageView = findViewById(R.id.imageView)
        buttonPicture = findViewById(R.id.buttonPicture)
        buttonPicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, requestCodeForPicture)
        }
        buttonSelect = findViewById(R.id.buttonSelect)
        buttonSelect.setOnClickListener {
            selectData()
            sampleDBAdapter.idList = arrayListId
            sampleDBAdapter.nameList = arrayListName
            sampleDBAdapter.notifyDataSetChanged()
        }
        buttonInsert = findViewById(R.id.buttonInsert)
        buttonInsert.setOnClickListener {
            var type = 1
            if (radioType2.isChecked) {
                type = 2
            }
            val bitmapDrawable = imageView.drawable as BitmapDrawable?
            if (bitmapDrawable != null) {
                insertData(editId.text.toString(), editName.text.toString(), type, bitmapDrawable.bitmap)
            }
        }
        buttonUpdate = findViewById(R.id.buttonUpdate)
        buttonUpdate.setOnClickListener {
            var type = 1
            if (radioType2.isChecked) {
                type = 2
            }
            val bitmapDrawable = imageView.drawable as BitmapDrawable?
            if (bitmapDrawable != null) {
                updateData(editId.text.toString(), editName.text.toString(), type, bitmapDrawable.bitmap)
            }
        }
        buttonDelete = findViewById(R.id.buttonDelete)
        buttonDelete.setOnClickListener {
            deleteData(editId.text.toString())
        }
        sampleDBAdapter = SampleDBAdapter(this)
        listView = findViewById(R.id.listView)
        listView.adapter = sampleDBAdapter
        listView.setOnItemClickListener { parent, view, position, id ->
            editId.setText(arrayListId.get(position), TextView.BufferType.NORMAL)
            editName.setText(arrayListName.get(position), TextView.BufferType.NORMAL)
            if (arrayListType.get(position) == 1) {
                radioType1.isChecked = true
            } else {
                radioType2.isChecked = true
            }
            imageView.setImageBitmap(arrayListBitmap.get(position))
        }
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodeForPicture && resultCode == Activity.RESULT_OK) {
            val inputStream = contentResolver.openInputStream(data?.data)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
            inputStream.close()
        }
    }
}
