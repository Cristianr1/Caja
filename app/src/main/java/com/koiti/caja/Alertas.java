package com.koiti.caja;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Alertas extends AppCompatActivity {

    private Context context;

    Alertas(Context context){
        this.context = context;
    }

    public AlertDialog alertaFechas() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Ha ocurrido un error")
                .setMessage("La fecha de salida no puede ser menor que la fecha de entrada")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

        return builder.create();
    }


    public AlertDialog alertaTarjetaBlanco() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Ha ocurrido un error")
                .setMessage("La tarjeta no tiene grabada la fecha de entrada.")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

        return builder.create();
    }

    public AlertDialog alertaTiempoSalida(int minutosRestantes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Tiempo para salir")
                .setMessage("Quedan "+minutosRestantes+" minutos para salir del parqueadero")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

        return builder.create();
    }

}
