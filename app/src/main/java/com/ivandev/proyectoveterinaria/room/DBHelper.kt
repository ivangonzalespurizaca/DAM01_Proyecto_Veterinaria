package com.ivandev.proyectoveterinaria.room

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ivandev.proyectoveterinaria.model.Especie
import com.ivandev.proyectoveterinaria.model.Raza
import com.ivandev.proyectoveterinaria.model.Usuario
import com.ivandev.proyectoveterinaria.model.Vacuna

class DBHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "VeterinariaApp.db"
        private const val DATABASE_VERSION = 2

        @Volatile
        private var INSTANCE: DBHelper? = null

        fun getInstance(context: Context): DBHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = DBHelper(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        // Tabla Usuarios
        const val TABLE_USUARIO = "Usuario"
        const val COL_UID = "uid"
        const val COL_NOMBRE = "nombre"
        const val COL_DNI = "dni"
        const val COL_CELULAR = "celular"
        const val COL_CORREO = "correo"
        const val COL_ROL = "rol"

        // Tabla Especie
        const val TABLE_ESPECIE = "Especie"
        const val COL_ESP_ID = "id"
        const val COL_ESP_NOMBRE = "nombre"
        const val COL_ESP_DEF = "definicion"
        const val COL_ESP_IMG = "imagen"

        // Tabla Raza
        const val TABLE_RAZA = "Raza"
        const val COL_RAZ_ID = "id"
        const val COL_RAZ_NOMBRE = "nombre"
        const val COL_RAZ_ESP_ID = "id_especie"

        // Tabla Vacuna
        const val TABLE_VACUNA = "Vacuna"
        const val COL_VAC_ID = "id_vacuna"
        const val COL_VAC_NOMBRE = "nombre_vacuna"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsuario = """
            CREATE TABLE $TABLE_USUARIO (
                $COL_UID TEXT PRIMARY KEY,
                $COL_NOMBRE TEXT,
                $COL_DNI TEXT,
                $COL_CELULAR TEXT,
                $COL_CORREO TEXT,
                $COL_ROL TEXT
            )
        """.trimIndent()

        val createEspecie = """
            CREATE TABLE $TABLE_ESPECIE (
                $COL_ESP_ID TEXT PRIMARY KEY,
                $COL_ESP_NOMBRE TEXT NOT NULL,
                $COL_ESP_DEF TEXT,
                $COL_ESP_IMG TEXT
            )
        """.trimIndent()

        val createRaza = """
            CREATE TABLE $TABLE_RAZA (
                $COL_RAZ_ID TEXT PRIMARY KEY,
                $COL_RAZ_NOMBRE TEXT NOT NULL,
                $COL_RAZ_ESP_ID TEXT,
                FOREIGN KEY($COL_RAZ_ESP_ID) REFERENCES $TABLE_ESPECIE($COL_ESP_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val createVacuna = """
            CREATE TABLE $TABLE_VACUNA (
                $COL_VAC_ID TEXT PRIMARY KEY,
                $COL_VAC_NOMBRE TEXT NOT NULL
            )
        """.trimIndent()

        db?.execSQL(createUsuario)
        db?.execSQL(createEspecie)
        db?.execSQL(createRaza)
        db?.execSQL(createVacuna)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIO")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ESPECIE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RAZA")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_VACUNA")
        onCreate(db)
    }

    // --- OPERACIONES USUARIO ---
    fun insertarUsuario(usuario: Usuario): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_UID, usuario.id)
            put(COL_NOMBRE, usuario.nombreCompleto)
            put(COL_DNI, usuario.dni)
            put(COL_CELULAR, usuario.celular)
            put(COL_CORREO, usuario.correo)
            put(COL_ROL, usuario.rol)
        }
        return db.insert(TABLE_USUARIO, null, values)
    }

    fun insertarEspecie(e: Especie): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_ESP_ID, e.id)
            put(COL_ESP_NOMBRE, e.nombre)
            put(COL_ESP_DEF, e.definicion)
            put(COL_ESP_IMG, e.imagen)
        }
        return db.insert(TABLE_ESPECIE, null, values)
    }

    fun listarEspecies(): List<Especie> {
        val lista = mutableListOf<Especie>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ESPECIE", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(Especie(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_ESP_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_ESP_NOMBRE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_ESP_DEF)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_ESP_IMG))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun actualizarEspecie(e: Especie): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_ESP_NOMBRE, e.nombre)
            put(COL_ESP_DEF, e.definicion)
            put(COL_ESP_IMG, e.imagen)
        }
        return db.update(TABLE_ESPECIE, values, "$COL_ESP_ID = ?", arrayOf(e.id))
    }

    fun eliminarEspecie(id: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_ESPECIE, "$COL_ESP_ID=?", arrayOf(id))
    }

    // --- OPERACIONES RAZA ---
    fun insertarRaza(r: Raza): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_RAZ_ID, r.id)
            put(COL_RAZ_NOMBRE, r.nombre)
            put(COL_RAZ_ESP_ID, r.idEspecie)
        }
        return db.insert(TABLE_RAZA, null, values)
    }

    fun listarRazasPorEspecie(idEspecie: String): List<Raza> {
        val lista = mutableListOf<Raza>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_RAZA WHERE $COL_RAZ_ESP_ID=?", arrayOf(idEspecie))
        if (cursor.moveToFirst()) {
            do {
                lista.add(Raza(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_RAZ_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_RAZ_NOMBRE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_RAZ_ESP_ID))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun actualizarRaza(r: Raza): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_RAZ_NOMBRE, r.nombre)
            put(COL_RAZ_ESP_ID, r.idEspecie)
        }
        return db.update(TABLE_RAZA, values, "$COL_RAZ_ID=?", arrayOf(r.id))
    }

    fun eliminarRaza(id: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_RAZA, "$COL_RAZ_ID=?", arrayOf(id))
    }

    // --- OPERACIONES VACUNA ---
    fun insertarVacuna(v: Vacuna): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_VAC_ID, v.idVacuna)
            put(COL_VAC_NOMBRE, v.nombreVacuna)
        }
        return db.insert(TABLE_VACUNA, null, values)
    }

    fun listarVacunas(): List<Vacuna> {
        val lista = mutableListOf<Vacuna>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_VACUNA", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(Vacuna(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_VAC_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_VAC_NOMBRE))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun actualizarVacuna(v: Vacuna): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_VAC_NOMBRE, v.nombreVacuna)
        }
        return db.update(TABLE_VACUNA, values, "$COL_VAC_ID=?", arrayOf(v.idVacuna))
    }

    fun eliminarVacuna(id: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_VACUNA, "$COL_VAC_ID=?", arrayOf(id))
    }
}