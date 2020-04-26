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

class CalculoSencillo {

    private SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getReadableDatabase();
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private LocalDateTime ldtEntrada, ldtLiquidacion;
    private String tfaCodigo, tdgLiquidacion;
    private int discountTime;

    CalculoSencillo(LocalDateTime ldtEntrada, LocalDateTime ldtLiquidacion, String tfaCodigo, String tdgLiquidacion, int discountTime) {
        this.ldtEntrada = ldtEntrada;
        this.ldtLiquidacion = ldtLiquidacion;
        this.tfaCodigo = tfaCodigo;
        this.tdgLiquidacion = tdgLiquidacion;
        this.discountTime = discountTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    int resultado() throws ParseException {
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

        int key = 1, cociente, cociente2;

        Date tdgLiq = formato.parse(tdgLiquidacion);
        ldtEntrada = ldtEntrada.plusMinutes(tdgLiq.getMinutes());

        int difMinutos = (int) Duration.between(ldtEntrada, ldtLiquidacion).toMinutes() - discountTime;
        difMinutos = negativeTimeSetting(difMinutos);
        Log.d("minutos", difMinutos + "");


        cociente = difMinutos / Objects.requireNonNull(mapBloques.get(1))[3]; //cociente entre la diferencia de minutos (entrada y liquidación) y el tiempo de subsegmentos
        if (cociente == 0) return 0;

        Log.d("cociente", cociente + "");

        while (cociente > 0) {
            datos = mapBloques.get(key);
            if (datos != null) {
                cociente2 = datos[0] / cociente;

                Log.d("cociente2", cociente2 + "");

                if (cociente2 > 0) {
                    resultado += cociente * datos[1];
                    cociente = 0;
                    Log.d("resultado", resultado + "---" + datos[1]);
                } else {
                    cociente = cociente - datos[0]; //minutosTotales es el número de agrupaciones que resultan de restar el numero de repeticiones con el cociente
                    resultado += datos[0] * datos[1];
                    Log.d("resultado", resultado + "-x--" + cociente + "--" + datos[0]);
                }
                key = datos[2];
            }
        }

//            while (difMinutos > 0) {
//                datos = mapBloques.get(key);
//                if (datos != null) {
//                    cociente = difMinutos / datos[3]; //cociente entre la diferencia de minutos (entrada y liquidación) y el tiempo de subsegmentos
//
//                    if (cociente == 0) {
//                        resultado += datos[1];
//                        difMinutos = 0;
//                    } else {
//                        minutosTotales = datos[0] - cociente; //minutosTotales es el número de agrupaciones que resultan de restar el numero de repeticiones con el cociente
//                        if (minutosTotales < 0) {
//                            resultado += datos[0] * datos[1];
//                            difMinutos = difMinutos - datos[0] * datos[3];
//                        } else {
//                            resultado += cociente * datos[1];
//                            difMinutos = difMinutos - datos[3];
//                        }
//                    }
//                    key = datos[2];
//                }
//            }

        return resultado;
    }

    private int negativeTimeSetting(int number) {
        return number < 0 ? 0 : number;
    }
}
