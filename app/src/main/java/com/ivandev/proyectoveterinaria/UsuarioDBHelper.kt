package com.ivandev.proyectoveterinaria

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ivandev.proyectoveterinaria.model.Usuario
import com.ivandev.proyectoveterinaria.model.Veterinario

class UsuarioDBHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "VeterinariaApp.db"
        private const val DATABASE_VERSION = 1

        // Tabla Usuarios (Común)
        const val TABLE_USUARIO = "Usuario"
        const val COL_UID = "uid"
        const val COL_NOMBRE = "nombre"
        const val COL_DNI = "dni"
        const val COL_CELULAR = "celular"
        const val COL_CORREO = "correo"
        const val COL_ROL = "rol"



        // Tabla Detalle Veterinario (Específica)
        const val TABLE_VET_DETALLE = "DetalleVeterinario"
        const val COL_VET_UID = "id_usuario"
        const val COL_COLEGIATURA = "num_colegiatura"
        const val COL_ESPECIALIDAD = "especialidad"
        const val COL_SEDE = "sede"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Tabla base para todos
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

        // Tabla de extensión para Veterinarios
        val createVetDetalle = """
            CREATE TABLE $TABLE_VET_DETALLE (
                $COL_VET_UID TEXT PRIMARY KEY,
                $COL_COLEGIATURA TEXT,
                $COL_SEDE TEXT,
                $COL_ESPECIALIDAD TEXT,
                FOREIGN KEY($COL_VET_UID) REFERENCES $TABLE_USUARIO($COL_UID) ON DELETE CASCADE
            )
        """.trimIndent()

        db?.execSQL(createUsuario)
        db?.execSQL(createVetDetalle)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIO")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_VET_DETALLE")
        onCreate(db)
    }

    fun insertarUsuario(usuario: Usuario): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_UID, usuario.idUsuario) // El ID que viene de Firebase
            put(COL_NOMBRE, usuario.nombresCompleto)
            put(COL_DNI, usuario.dni)
            put(COL_CELULAR, usuario.celular)
            put(COL_CORREO, usuario.correo)
            put(COL_ROL, usuario.rol)
        }

        val resultado = db.insert(TABLE_USUARIO, null, values)
        db.close()
        return resultado
    }

    fun insertarVeterinarioCompleto(usuario: Usuario, veterinario: Veterinario): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            val valuesUser = ContentValues().apply {
                put(COL_UID, usuario.idUsuario)
                put(COL_NOMBRE, usuario.nombresCompleto)
                put(COL_DNI, usuario.dni)
                put(COL_CELULAR, usuario.celular)
                put(COL_CORREO, usuario.correo)
                put(COL_ROL, "VETERINARIO")
            }
            db.insertOrThrow(TABLE_USUARIO, null, valuesUser)

            val valuesVet = ContentValues().apply {
                put(COL_VET_UID, usuario.idUsuario)
                put(COL_COLEGIATURA, veterinario.numColegiatura)
                put(COL_SEDE, veterinario.sede)
                put(COL_ESPECIALIDAD, veterinario.especialidad)
            }
            db.insertOrThrow(TABLE_VET_DETALLE, null, valuesVet)

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            return false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun obtenerUsuarioPorCorreo(correo: String): Usuario? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USUARIO,
            null,
            "$COL_CORREO = ?",
            arrayOf(correo),
            null, null, null
        )

        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = Usuario(
                idUsuario = cursor.getString(cursor.getColumnIndexOrThrow(COL_UID)),
                nombresCompleto = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE)),
                dni = cursor.getString(cursor.getColumnIndexOrThrow(COL_DNI)),
                celular = cursor.getString(cursor.getColumnIndexOrThrow(COL_CELULAR)),
                correo = cursor.getString(cursor.getColumnIndexOrThrow(COL_CORREO)),
                rol = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROL))
            )
        }
        cursor.close()
        db.close()
        return usuario
    }
}