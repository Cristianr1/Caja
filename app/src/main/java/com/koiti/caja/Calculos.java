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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Calculos {
    private SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getReadableDatabase();
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private LocalDateTime ldtEntrada, ldtLiquidacion, ldtDiaSiguiente;
    private String[] strDias = new String[]{"Lunes", "Martes", "Mi�rcoles", "Jueves",
            "Viernes", "S�bado", "Domingo"};
    private Thread Thread1 = null;

    private CalSubsegmentos subsegmentos = new CalSubsegmentos();

    public Calculos(LocalDateTime ldtEntrada, LocalDateTime ldtLiquidacion) {
        this.ldtEntrada = ldtEntrada;
        this.ldtLiquidacion = ldtLiquidacion;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int calculoEntrada(String tfa_codigo, String tdgLiquidacion, boolean mDia) throws ParseException {
        String tsgHoraDesde, tsgHoraHasta, tsgCodigoDb, diaDesde, diaHasta;
        String diaEntrada = strDias[ldtEntrada.getDayOfWeek().ordinal()];
        int tiempo = 0;
        int resultado = 0;

        Cursor c = db.query(
                "tb_tarifa_segmentos",
                null,
                "tsg_tfa_codigo" + " LIKE ?" + " AND " + "tsg_dia_desde" + " LIKE ?",
                new String[]{tfa_codigo, diaEntrada},
                null,
                null,
                null);

        while (c.moveToNext()) {
            tsgHoraDesde = c.getString(c.getColumnIndex("tsg_hora_desde"));
            tsgHoraHasta = c.getString(c.getColumnIndex("tsg_hora_hasta"));
            tsgCodigoDb = c.getString(c.getColumnIndex("tsg_codigo"));
            diaDesde = c.getString(c.getColumnIndex("tsg_dia_desde"));
            diaHasta = c.getString(c.getColumnIndex("tsg_dia_hasta"));

            Date horaDesde = formato.parse(tsgHoraDesde);
            Date horaHasta = formato.parse(tsgHoraHasta);
            Date tdgLiq = formato.parse(tdgLiquidacion);

            ldtEntrada = ldtEntrada.plusMinutes(tdgLiq.getMinutes());

            LocalDate desde = ldtEntrada.query(TemporalQueries.localDate());

            LocalDateTime ldtdesde = desde.atTime(horaDesde.getHours(), horaDesde.getMinutes());
            LocalDateTime ldthasta = desde.atTime(horaHasta.getHours(), horaHasta.getMinutes());

            ldtdesde = ldtdesde.plusMinutes(tdgLiq.getMinutes());
            ldthasta = ldthasta.plusMinutes(tdgLiq.getMinutes());

            if (!diaDesde.equals(diaHasta))
                ldthasta = desde.atTime(23, 59);

            if (ldtEntrada.isBefore(ldtdesde) && c.isFirst()) {
                resultado += anteriorFecha(tfa_codigo, tdgLiq)[0];
                tiempo = anteriorFecha(tfa_codigo, tdgLiq)[1];
                ldtdesde = ldtEntrada.plusMinutes(tiempo);
                Log.d("anterior", resultado + "---" + ldtEntrada + "--->" + ldtdesde + "--->" + tiempo);
            }

            if (ldtEntrada.isAfter(ldtdesde)) {
                if (ldtLiquidacion.isBefore(ldthasta)) {
                    int difMinutos = (int) Duration.between(ldtEntrada, ldtLiquidacion).toMinutes();
                    if (difMinutos < 0) difMinutos = 0;
                    resultado += subsegmentos.getEntrada(tfa_codigo, tsgCodigoDb, difMinutos)[0];
                    Log.d("entradaAntes", difMinutos + "---->" + tsgCodigoDb + "---->" + resultado);
                    break;
                } else {
                    int difMinutos = (int) Duration.between(ldtEntrada, ldthasta).toMinutes();
                    if (difMinutos > 0) {
                        resultado += subsegmentos.getEntrada(tfa_codigo, tsgCodigoDb, difMinutos)[0];
                        tiempo = subsegmentos.getEntrada(tfa_codigo, tsgCodigoDb, difMinutos)[1];
                    }
                    Log.d("entradaDespues", difMinutos + "---->" + tsgCodigoDb + "---->" + resultado);
                }
            } else {
                int difMinutos = (int) Duration.between(ldtdesde, ldtLiquidacion).toMinutes();
                if (difMinutos < 0) difMinutos = 0;
                resultado += subsegmentos.getEntrada(tfa_codigo, tsgCodigoDb, difMinutos)[0];
                tiempo = subsegmentos.getEntrada(tfa_codigo, tsgCodigoDb, difMinutos)[1];
                Log.d("entradaDespues2", difMinutos + "---->" + tsgCodigoDb + "---->" + resultado);
            }
            Log.d("entrada", diaDesde + "--->" + ldtdesde + "---" + diaHasta + "--->" + ldthasta);

            if (mDia && c.isLast() && ldtdesde.isBefore(ldtEntrada)) {
                ldtDiaSiguiente = ldtEntrada.plusMinutes(tiempo);
            } else if (mDia && c.isLast()) {
                ldtDiaSiguiente = ldtdesde.plusMinutes(tiempo + 1);
            }
        }
        c.close();
        return resultado;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    int calculoSalida(String tfa_codigo, String tdgLiquidacion) throws ParseException {
        String tsgHoraDesde, tsgHoraHasta, tsgCodigoDb, diaDesde, diaHasta;
        String diaEntrada = strDias[ldtLiquidacion.getDayOfWeek().ordinal()];
        int resultado = 0;

        Cursor c = db.query(
                "tb_tarifa_segmentos",
                null,
                "tsg_tfa_codigo" + " LIKE ?" + " AND " + "tsg_dia_desde" + " LIKE ?",
                new String[]{tfa_codigo, diaEntrada},
                null,
                null,
                null);

        while (c.moveToNext()) {
            tsgHoraDesde = c.getString(c.getColumnIndex("tsg_hora_desde"));
            tsgHoraHasta = c.getString(c.getColumnIndex("tsg_hora_hasta"));
            tsgCodigoDb = c.getString(c.getColumnIndex("tsg_codigo"));
            diaDesde = c.getString(c.getColumnIndex("tsg_dia_desde"));
            diaHasta = c.getString(c.getColumnIndex("tsg_dia_hasta"));

            Date horaDesde = formato.parse(tsgHoraDesde);
            Date horaHasta = formato.parse(tsgHoraHasta);
            Date tdgLiq = formato.parse(tdgLiquidacion);

            LocalDate desde = ldtLiquidacion.query(TemporalQueries.localDate());

            LocalDateTime ldtdesde = desde.atTime(horaDesde.getHours(), horaDesde.getMinutes());
            LocalDateTime ldthasta = desde.atTime(horaHasta.getHours(), horaHasta.getMinutes());

            ldtdesde = ldtdesde.plusMinutes(tdgLiq.getMinutes());
            ldthasta = ldthasta.plusMinutes(tdgLiq.getMinutes());

            if (ldtDiaSiguiente.isAfter(ldtdesde) && c.isFirst()) {
                ldtdesde = ldtDiaSiguiente;
            }

            if (!diaDesde.equals(diaHasta))
                ldthasta = desde.atTime(23, 59);

            if (ldtLiquidacion.isAfter(ldtdesde)) {
                if (ldtLiquidacion.isBefore(ldthasta)) {
                    int difMinutos = (int) Duration.between(ldtdesde, ldtLiquidacion).toMinutes();
                    resultado += subsegmentos.getEntrada(tfa_codigo, tsgCodigoDb, difMinutos)[0];
                    Log.d("salida 2", difMinutos + "---->" + ldtdesde + "---->" + ldthasta + "--->" + resultado);
                } else {
                    int difMinutos = (int) Duration.between(ldtdesde, ldthasta).toMinutes();
                    resultado += subsegmentos.getEntrada(tfa_codigo, tsgCodigoDb, difMinutos)[0];
                    Log.d("salida 1", difMinutos + "---->" + ldtdesde + "---->" + ldthasta + "--->" + resultado);
                }
            }
            Log.d("liquidacion", ldtDiaSiguiente + "--->" + ldtdesde + "---" + diaHasta + "--->" + ldthasta + "--->" + tsgCodigoDb);
        }
        c.close();

        return resultado;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int calculoDiaIntermedio(String tfa_codigo) throws ParseException {
        ArrayList<String> tsgCodigo = new ArrayList<>();
        String tsgHoraDesde, tsgCodigoDb;
        int resultado = 0;

        ldtEntrada = ldtEntrada.plusDays(1);

        int dias = (int) Duration.between(ldtEntrada.withHour(0).withMinute(0), ldtLiquidacion.withHour(0).withMinute(0)).toDays();

        while (dias > 0) {
            String diaSemana = strDias[ldtEntrada.getDayOfWeek().ordinal()];

            Cursor cDiaSemana = db.query(
                    "tb_tarifa_segmentos",
                    null,
                    "tsg_tfa_codigo" + " LIKE ?" + " AND " + "tsg_dia_desde" + " LIKE ?",
                    new String[]{tfa_codigo, diaSemana},
                    null,
                    null,
                    null);

            while (cDiaSemana.moveToNext()) {
                tsgHoraDesde = cDiaSemana.getString(cDiaSemana.getColumnIndex("tsg_hora_desde"));
                tsgCodigoDb = cDiaSemana.getString(cDiaSemana.getColumnIndex("tsg_codigo"));
                tsgCodigo.add(tsgCodigoDb);
                Collections.sort(tsgCodigo);

                Date horaDesde = formato.parse(tsgHoraDesde);
                LocalDate desde = ldtLiquidacion.query(TemporalQueries.localDate());

                LocalDateTime ldtdesde = desde.atTime(horaDesde.getHours(), horaDesde.getMinutes());

                int tiempo = subsegmentos.getEntrada(tfa_codigo, tsgCodigoDb, 0)[1];

                if (dias == 1 && cDiaSemana.isLast()) {
                    ldtDiaSiguiente = ldtdesde.plusMinutes(tiempo + 1);
                    ldtDiaSiguiente = ldtDiaSiguiente.minusDays(1);
                    ldtDiaSiguiente = ldtDiaSiguiente.plusMinutes(10);
                }
            }

            resultado += subsegmentos.getDiaCompleto("T1", tsgCodigo);

            Log.d("dias intermedios", diaSemana + "--->" + resultado);


            tsgCodigo.clear();
            cDiaSemana.close();

            ldtEntrada = ldtEntrada.plusDays(1);
            dias = (int) Duration.between(ldtEntrada.withHour(0).withMinute(0), ldtLiquidacion.withHour(0).withMinute(0)).toDays();

        }
        return resultado;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int[] anteriorFecha(String tfa_codigo, Date tdgLiq) {
        String tsgHoraHasta, tsgCodigoDb, diaDesde, diaHasta, tdgLiquidacion;

        LocalDateTime today = ldtEntrada;
        LocalDateTime yesterday = ldtEntrada.minusDays(1);

        String sToday = strDias[today.getDayOfWeek().ordinal()];
        String sYesterday = strDias[yesterday.getDayOfWeek().ordinal()];

        int resultado = 0, tiempo = 0;

        Cursor c = db.query(
                "tb_tarifa_segmentos",
                null,
                "tsg_tfa_codigo" + " LIKE ?" + " AND " + "tsg_dia_desde" + " LIKE ?" + " AND " + "tsg_dia_hasta" + " LIKE ?",
                new String[]{"T1", sYesterday, sToday},
                null,
                null,
                null);

        while (c.moveToNext()) {
            tsgHoraHasta = c.getString(c.getColumnIndex("tsg_hora_hasta"));
            tsgCodigoDb = c.getString(c.getColumnIndex("tsg_codigo"));

            try {
                Date horaHasta = formato.parse(tsgHoraHasta);

                LocalDate desde = ldtEntrada.query(TemporalQueries.localDate());

                LocalDateTime ldthasta = desde.atTime(horaHasta.getHours(), horaHasta.getMinutes());

                ldthasta = ldthasta.plusMinutes(tdgLiq.getMinutes());

                int difMinutos = (int) Duration.between(ldtEntrada, ldthasta).toMinutes();

                resultado += subsegmentos.getEntrada(tfa_codigo, tsgCodigoDb, difMinutos)[0];
                tiempo = subsegmentos.getEntrada(tfa_codigo, tsgCodigoDb, difMinutos)[1];

//                ldtEntrada = ldtEntrada.plusMinutes(subsegmentos.getEntrada(tfa_codigo, tsgCodigoDb, difMinutos)[1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        c.close();

        return new int[]{resultado, tiempo};
    }
}
