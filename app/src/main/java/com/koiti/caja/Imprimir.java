package com.koiti.caja;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

import wangpos.sdk4.libbasebinder.BankCard;
import wangpos.sdk4.libbasebinder.Printer;
import wangpos.sdk4.libbasebinder.Printer.Align;


public class Imprimir {

    private Context context;
    private Printer mPrinter;

    private String vehId, fechaEntrada, tfaCodigo, tfaNombre, facturaFHEmision, facturaEmision, facturaNumero, usuario,
            fechaInicioSesion, estacion, prefijo;
    private int horas = 0, minutos;
    private String total, subtotal, impuesto,recibido, cambio;

    private boolean bloop = false;
    private boolean bthreadrunning = false;

    @SuppressLint("DefaultLocale")
    public Imprimir(Context context, String vehId, String fechaEntrada, String tfaCodigo, String tfaNombre, String facturaNumero, String facturaFHEmision, String facturaEmision,
                    String usuario, String estacion, String fechaInicioSesion, String prefijo, float total, float subtotal, float impuesto,
                    int minutos, int recibido, int cambio) {
        this.context = context;
        this.vehId = vehId;
        this.fechaEntrada = fechaEntrada;
        this.tfaCodigo = tfaCodigo;
        this.tfaNombre = tfaNombre;
        this.facturaNumero = facturaNumero;
        this.facturaFHEmision = facturaFHEmision;
        this.facturaEmision = facturaEmision;
        this.usuario = usuario;
        this.estacion = estacion;
        this.fechaInicioSesion = fechaInicioSesion;
        this.prefijo = prefijo;
        this.total = String.format("%,.2f", total);
        this.subtotal = String.format("%,.2f", subtotal);
        this.impuesto = String.format("%,.2f", impuesto);
        this.minutos = minutos;
        this.recibido = String.format("%,.2f", (float) recibido);
        this.cambio = String.format("%,.2f", (float) cambio);
    }


    public void imprimirFactura() {
        new Thread() {
            @Override
            public void run() {
                mPrinter = new Printer(context);
                try {
                    mPrinter.setPrintFontType(context, "");//fonnts/PraduhhTheGreat.ttf
                    horas = minutos / 60;
                    minutos = minutos % 60;

//                    Log.d("datosFactura", facturaNumero+"-->"+recibido+"-->"+cambio);
                    if (!bthreadrunning)
                        new PrintThread().start();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void testPrintString(int result) {

        try {
            //default content print
            mPrinter.printString("------------------------------------------", 30, Align.CENTER, true, false);
            mPrinter.print2StringInLine("Factura de Venta", facturaNumero, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.print2StringInLine("Fecha", facturaEmision, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.printString("------------------------------------------", 30, Align.CENTER, true, false);
            mPrinter.printString("Concepto: PARQUEO -Reliquidado", 22, Align.LEFT, true, false);
            mPrinter.print2StringInLine("Placa/Código:", vehId, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.print2StringInLine("Entrada:", fechaEntrada, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.print2StringInLine("Salida:", facturaFHEmision, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.print2StringInLine("Tiempo:", horas + ":" + minutos, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.print2StringInLine("Tarifa:", tfaNombre, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.printString("------------------------------------------", 30, Align.CENTER, true, false);
            mPrinter.print2StringInLine("Cnt Descripción", "Subtotal", (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.printString("------------------------------------------", 30, Align.CENTER, true, false);
            mPrinter.print2StringInLine("1 Parqueadero", "$" + total, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.printString("--------------", 30, Align.RIGHT, true, false);
            mPrinter.print2StringInLine("Subtotal:", "$" + total, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.print2StringInLine("Ajuste:", "$ 0.0", (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.printString("--------------", 30, Align.RIGHT, true, false);
            mPrinter.printString("--------------", 30, Align.RIGHT, true, false);
            mPrinter.print2StringInLine("TOTAL A PAGAR  ==>", "$"+total, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.printString("------------------------------------------", 30, Align.CENTER, true, false);
            mPrinter.printString("DESGLOSE DE SERVICIO", 22, Align.LEFT, true, false);
            mPrinter.printString("------------------------------------------", 30, Align.CENTER, true, false);
            mPrinter.print2StringInLine("Base Servicio:", "$"+subtotal, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.print2StringInLine("Impu: 19% IVA", "$"+impuesto, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.print2StringInLine("TOTAL A PAGAR  ==>", "$"+total, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.printString("------------------------------------------", 30, Align.CENTER, true, false);
            mPrinter.printString("Forma de Pago: Efectivo", 22, Align.LEFT, true, false);
            mPrinter.print2StringInLine("Recibido:", "$"+recibido, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
            mPrinter.print2StringInLine("Cambio:", "$"+cambio, (float) 1.5, Printer.Font.SERIF, 22,
                    Align.CENTER, true, false, false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public class PrintThread extends Thread {
        @Override
        public void run() {
            bthreadrunning = true;
            int datalen = 0;
            int result = 0;
            byte[] senddata = null;
            do {
                try {
                    result = mPrinter.printInit();
                    //clear print cache
                    mPrinter.clearPrintDataCache();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                try {

//                    testPrintImageBase("logo");
                    // Print text
                    testPrintString(result);

                    //print end reserve height
                    result = mPrinter.printPaper(100);
                    //Detecting the in-place status of the card during printing
//                    mPrinter.printPaper_trade(5,100);
                    result = mPrinter.printFinish();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (bloop);
            bthreadrunning = false;
        }
    }

}
