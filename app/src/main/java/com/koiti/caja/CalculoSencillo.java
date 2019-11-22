package com.koiti.caja;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class CalculoSencillo {

    private SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getReadableDatabase();
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private LocalDateTime ldtEntrada, ldtLiquidacion;
    private String tfaCodigo, tdgLiquidacion;

    public CalculoSencillo(LocalDateTime ldtEntrada, LocalDateTime ldtLiquidacion, String tfaCodigo, String tdgLiquidacion) {
        this.ldtEntrada = ldtEntrada;
        this.ldtLiquidacion = ldtLiquidacion;
        this.tfaCodigo = tfaCodigo;
        this.tdgLiquidacion = tdgLiquidacion;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int resultado() throws ParseException {
        String bloquesRepetir, bloquesValor, bloquesCodigo, bloquesTiempo, bloqueSiguiente;
        int resultado = 0;
        int[] datos;
        Map<Integer, int[]> mapBloques = new TreeMap<>();

        Cursor c = db.query(
                "tb_tarifa_subsegmentos",
                null,
                "tss_tfa_codigo" + " LIKE ?",
                new String[]{tfaCodigo},
                null,
                null,
                null);

        while (c.moveToNext()) {
            bloquesRepetir = c.getString(c.getColumnIndex("tss_repetir"));
            bloquesValor = c.getString(c.getColumnIndex("tss_valor"));
            bloquesCodigo = c.getString(c.getColumnIndex("tss_codigo"));
            bloquesTiempo = c.getString(c.getColumnIndex("tss_tiempo"));
            bloqueSiguiente = c.getString(c.getColumnIndex("tss_siguiente"));

            Date tiempo = formato.parse(bloquesTiempo);
            mapBloques.put(Integer.parseInt(bloquesCodigo),
                    new int[]{Integer.parseInt(bloquesRepetir),
                            (int) Double.parseDouble(bloquesValor),
                            Integer.parseInt(bloqueSiguiente),
                            tiempo.getHours() * 60 + tiempo.getMinutes()});


        }
        c.close();

        int key = 1, minutosTotales, cociente;

        Date tdgLiq = formato.parse(tdgLiquidacion);
        ldtEntrada = ldtEntrada.plusMinutes(tdgLiq.getMinutes());

        int difMinutos = (int) Duration.between(ldtEntrada, ldtLiquidacion).toMinutes();

            while (difMinutos > 0) {
                datos = mapBloques.get(key);
                if (datos != null) {
                    cociente = difMinutos / datos[3]; //cociente entre la diferencia de minutos (entrada y liquidación) y el tiempo de subsegmentos

                    if (cociente == 0) {
                        resultado += datos[1];
                        difMinutos = 0;
                    } else {
                        minutosTotales = datos[0] - cociente; //minutosTotales es el número de agrupaciones que resultan de restar el numero de repeticiones con el cociente
                        if (minutosTotales < 0) {
                            resultado += datos[0] * datos[1];
                            difMinutos = difMinutos - datos[0] * datos[3];
                        } else {
                            resultado += cociente * datos[1];
                            difMinutos = difMinutos - datos[3];
                        }
                    }
                    key = datos[2];
                }
            }

        return resultado;
    }
}
