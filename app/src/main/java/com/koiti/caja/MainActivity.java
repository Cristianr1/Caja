package com.koiti.caja;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private Button liqTarifa, box;
    private TextView valor, valorSubtotal, valorImpuestos, datos, cambio, recibido, dineroCierre, dineroApertura, valorPagar;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateH = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
    private String fechaHActual, fechaActual, usuario, fechaInicioSesion, estacion, prefijo, fechaEntrada, facturaNumero,
            vehId, tfaCodigo, tfaNombre, type;
    private int acumulador; //Acumulador de la factura
    private int code, id, dineroCierreCaja = 0, dineroCaja = 0;

    StringBuilder resultadoSubtotal = new StringBuilder("$");
    StringBuilder resultadoImpuesto = new StringBuilder("$");
    StringBuilder resultadoFinal = new StringBuilder("$");
    int resultadoAjuste = 0, numEntregado = 0, numCambio = 0;
    float vsubtotal, vimpuestos;
    private Boolean active = false, print = false, cajaInicial;
    private EditText moneyOpen, moneyClose;

    private LocalDateTime ldtEntrada, ldtLiquidacion;

    private NfcAdapter nfcAdapter;

    Tarifas tarifas = new Tarifas();
    ConfigStorage configuracion = new ConfigStorage();


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        context = this;

        Toasty.Config.getInstance().setTextSize(24).apply();

        liqTarifa = findViewById(R.id.liquidacion_Id);
        box = findViewById(R.id.caja_Id);
        valorSubtotal = findViewById(R.id.subtotal);
        valorImpuestos = findViewById(R.id.impuestos);
        valor = findViewById(R.id.valorLiquidacion);
        datos = findViewById(R.id.datos_id);

        usuario = configuracion.getValueString("usuario", context);
        estacion = configuracion.getValueString("estacion", context);
        prefijo = configuracion.getValueString("prefijo", context);

        code = configuracion.getValueInt("code", context);
        id = configuracion.getValueInt("id", context);

        cajaInicial = configuracion.getValueBoolean("apertura" + usuario, context);

        StringBuilder data = new StringBuilder("Usuario: ");
        data.append(usuario);
        data.append("\t\t\t");
        data.append("Estación: ");
        data.append(estacion);

        datos.setText(data);
        // Register the onClick listener with the implementation above
        liqTarifa.setOnClickListener(mListener);
        box.setOnClickListener(mListener);

        disableBox();
        Sesiones inicioSesionFH = new Sesiones(usuario);
        fechaInicioSesion = inicioSesionFH.fhInicioSesion();

        final TextView textViewDate = findViewById(R.id.fechahora_id);

        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fechaHActual = dateH.format(new Date());
                fechaActual = date.format(new Date());
                textViewDate.setText(fechaHActual);
                someHandler.postDelayed(this, 1000);
            }
        }, 10);

        if (!cajaInicial) {
            aperturaCaja();
        }
    }


    private View.OnClickListener mListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.liquidacion_Id:
                    liqTarifa.setBackground(getDrawable(R.drawable.btn_round_green));//Select Color
                    active = true;
                    break;
                case R.id.caja_Id:
                    alertCaja();
                    break;
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            return;
        }
        Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Mifare mifare = new Mifare(nfcTag);

        final Alertas alerta = new Alertas(context);
        int index;

        if (active) {
            resultadoSubtotal.delete(1, resultadoSubtotal.length());
            resultadoImpuesto.delete(1, resultadoImpuesto.length());
            resultadoFinal.delete(1, resultadoFinal.length());
            switch (mifare.connectTag()) {
                case Mifare.MIFARE_CONNECTION_SUCCESS:
                    if (mifare.authentificationKey(Mifare.KOITI_KEY1, Mifare.KEY_TYPE_A, 1)) {
                        byte[] datosB0 = mifare.readMifareTagBlock(1, 0);
                        byte[] datosB1 = mifare.readMifareTagBlock(1, 1);
                        byte[] datosB2 = mifare.readMifareTagBlock(1, 2);

                        if (datosB0 != null && datosB1 != null && datosB2 != null) {
                            if (datosB1[0] == 1 && datosB1[1] == code && datosB1[3] == id) {
                                String sRead = new String(datosB0);
                                vehId = sRead.replaceAll("[^\\x20-\\x7e]", "");

                                if (datosB2[0] == 0)
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            alerta.alertaTarjetaBlanco().show();
                                        }
                                    });
                                else {
                                    DecimalFormat doble0 = new DecimalFormat("00");

                                    fechaEntrada = "20" + doble0.format(datosB2[0]) + "-" + doble0.format(datosB2[1]) + "-" + doble0.format(datosB2[2]) +
                                            " " + doble0.format(datosB2[3]) + ":" + doble0.format(datosB2[4]);

                                    if (datosB2[8] == 0) {
                                        index = configuracion.getValueInt("carro_tfa_codigo", context);
                                        type = "Carro";
                                    } else if (datosB2[8] == 1) {
                                        index = configuracion.getValueInt("moto_tfa_codigo", context);
                                        type = "Moto";
                                    } else {
                                        index = configuracion.getValueInt("bici_tfa_codigo", context);
                                        type = "Bicicleta";
                                    }

                                    ldtEntrada = LocalDateTime.of(datosB2[0] + 2000, datosB2[1], datosB2[2], datosB2[3], datosB2[4]);
                                    liquidacionCaja(ldtEntrada, index);

                                    enableBox();
                                }
                            } else {
                                Toasty.error(getBaseContext(), "" + "Tarjeta no pertenece al" +
                                        " parqueadero.", Toast.LENGTH_LONG).show();
                            }
                            liqTarifa.setBackground(getDrawable(R.drawable.btn_round));//Default Color
                            active = false;
                        } else {
                            Toasty.error(getBaseContext(), "" + "La lectura ha fallado" +
                                    " por favor vuelva a intentarlo.", Toast.LENGTH_LONG).show();
                        }

                    } else
                        Toast.makeText(getBaseContext(), "Fallo de autentificación", Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            Toasty.warning(getBaseContext(), "Presione primero el botón liquidación", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MainActivity.class).
                addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        IntentFilter[] intentFilter = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                intentFilter, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @SuppressLint("DefaultLocale")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void liquidacionCaja(LocalDateTime ldtEntrada, int index) {

        ArrayList<String> tfacodigo = tarifas.tfaCodigo();
        ArrayList<String> tfanombre = tarifas.tfaNombre();

        final Alertas alerta = new Alertas(context);

        int ajuste = tarifas.tfaAjuste(tfacodigo.get(index));
        String tdgLiquidacion = tarifas.tdgLiquidacion(tfacodigo.get(index));
        tfaCodigo = tfacodigo.get(index);
        tfaNombre = tfanombre.get(index);

        int resultadoCaja = 0;
//            tdgSalida = T1.getString(T1.getColumnIndex("tfa_tiempo_gracia_sal"));

//        LocalDateTime ldtEntrada = LocalDateTime.of(2019, 10, 8, 21, 41); //Fecha de entrada (grabada en la tarjeta)
//        ldtLiquidacion = LocalDateTime.of(2019, 11, 5, 9, 0); //Fecha actual
        ldtLiquidacion = LocalDateTime.now();//Fecha actual

        Calculos liquidacion = new Calculos(ldtEntrada, ldtLiquidacion);

        GetSegmentos segmentos = new GetSegmentos(tfaCodigo);

        int seg = segmentos.numSegmentos();

        if (seg == 1) {
            CalculoSencillo sencillo = new CalculoSencillo(ldtEntrada, ldtLiquidacion, tfaCodigo);
            resultadoCaja = sencillo.resultado();
        } else {
            int difDias = (int) Duration.between(ldtEntrada.withHour(0).withMinute(0), ldtLiquidacion.withHour(0).withMinute(0)).toDays();


            LocalDateTime quemado1 = LocalDateTime.of(2019, 3, 4, 16, 10); //Fecha actual
            LocalDateTime quemado2 = LocalDateTime.of(2019, 3, 5, 8, 10); //Fecha actual

            int difQuemados = (int) Duration.between(quemado1.withHour(0).withMinute(0), quemado2.withHour(0).withMinute(0)).toDays();

            Log.d("difQuemados", String.valueOf(difQuemados) + "---" + difDias);

            if (difDias < 0) {
                alerta.alertaFechas().show();
            } else if (difDias == 0) {
                try {
                    resultadoCaja += liquidacion.calculoEntrada(tfaCodigo, tdgLiquidacion, false);
                    Log.d("resultadoCaja", resultadoCaja + "");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if (difDias == 1) {
                try {
                    resultadoCaja += liquidacion.calculoEntrada(tfaCodigo, tdgLiquidacion, true);
                    resultadoCaja += liquidacion.calculoSalida(tfaCodigo, tdgLiquidacion);
                    Log.d("resultadoCaja", resultadoCaja + "");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    resultadoCaja += liquidacion.calculoEntrada(tfaCodigo, tdgLiquidacion, false);
                    resultadoCaja += liquidacion.calculoDiaIntermedio(tfaCodigo);
                    resultadoCaja += liquidacion.calculoSalida(tfaCodigo, tdgLiquidacion);
                    Log.d("resultadoCaja", resultadoCaja + "");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        resultadoAjuste = (int) Math.round(resultadoCaja / (ajuste * 1.0)) * ajuste;
        if (resultadoAjuste < resultadoCaja) resultadoAjuste = resultadoAjuste + ajuste;

        Log.d("resultadoCaja", resultadoAjuste + "");

        vsubtotal = (float) (resultadoAjuste / (1.19));
        vimpuestos = resultadoAjuste - vsubtotal;

        resultadoSubtotal.append(String.format("%,d", (int) vsubtotal));
        resultadoImpuesto.append(String.format("%,d", (int) vimpuestos));
        resultadoFinal.append(String.format("%,d", resultadoAjuste));

        valorSubtotal.setText(resultadoSubtotal);
        valorImpuestos.setText(resultadoImpuesto);
        valor.setText(resultadoFinal);

    }


    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            intent = new Intent(context, Password.class);
            startActivity(intent);
        } else if (id == R.id.action_logout) {
            cierreCaja();
        } else if (id == R.id.action_close) {
            finish();
        } else {
            if (ldtEntrada != null && print) {
                int difMinutos = (int) Duration.between(ldtEntrada, ldtLiquidacion).toMinutes();
                Imprimir imprimir = new Imprimir(context, vehId, fechaEntrada, tfaCodigo, tfaNombre, facturaNumero, fechaHActual, fechaActual, usuario,
                        estacion, fechaInicioSesion, prefijo, resultadoAjuste, vsubtotal, vimpuestos, difMinutos, numEntregado, numCambio);
                imprimir.imprimirFactura();
                print = false;
            } else if (!print) {
                Toasty.info(getBaseContext(), "Para poder imprimir finalice caja", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void alertCaja() {
        acumulador = configuracion.getValueInt("next", context);
        facturaNumero = String.valueOf(acumulador + configuracion.getValueInt("start", context));

        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_box, null);

        final EditText money = dialogView.findViewById(R.id.editText);
        recibido = dialogView.findViewById(R.id.entregado_id);
        valorPagar = dialogView.findViewById(R.id.valorPagar_id);
        cambio = dialogView.findViewById(R.id.cambio_id);
        Button ok = dialogView.findViewById(R.id.button);

        valorPagar.setText(resultadoFinal);

        money.addTextChangedListener(listenerMoney);

        ok.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                final GuardarDatos guardarDb = new GuardarDatos(vehId, fechaEntrada, tfaCodigo, tfaNombre, facturaNumero, fechaHActual, fechaActual, usuario,
                        estacion, fechaInicioSesion, prefijo, resultadoAjuste, vsubtotal, vimpuestos, numEntregado, numCambio, type);

                valorSubtotal.setText("$0");
                valorImpuestos.setText("$0");
                valor.setText("$0");

                guardarDb.tbFacturas();
                guardarDb.tbFacturaDetalle();
                guardarDb.tbFacturaImpuestos();
                guardarDb.tbLiquidaciones();
                guardarDb.tbCaja();
                guardarDb.tbVehiculos();

                resultadoSubtotal.delete(1, resultadoSubtotal.length());
                resultadoImpuesto.delete(1, resultadoImpuesto.length());
                resultadoFinal.delete(1, resultadoFinal.length());

                disableBox();
                configuracion.save(++acumulador, "next", context);
                dialogBuilder.dismiss();
                print = true;
//                Toast.makeText(getBaseContext(), "" + "Liquidación Exitosa", Toast.LENGTH_LONG).show();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void cierreCaja() {
        final Intent[] intent = new Intent[1];

        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_close_box, null);

        moneyClose = dialogView.findViewById(R.id.dineroFinalInput_id);
        dineroCierre = dialogView.findViewById(R.id.dinerofinal_id);
        Button ok = dialogView.findViewById(R.id.ok_id);

        moneyClose.addTextChangedListener(listenerMoney);

        dineroCaja = configuracion.getValueInt("dinero_caja"+usuario, context);

        ok.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
                configuracion.save(false, "sesion", context);
                configuracion.save(false, "apertura" + usuario, context);
                SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getWritableDatabase();
                ContentValues registerUpdate = new ContentValues();
                registerUpdate.put("ses_fh_final", fechaHActual);
                registerUpdate.put("ses_dinero_reportado", dineroCierreCaja);
                registerUpdate.put("ses_diferencia", (dineroCierreCaja - dineroCaja));
                String updateSentence = "ses_usuario" + " LIKE ?" + " AND " + "ses_fh_final" + " IS NULL";
                db.update("tb_sesiones", registerUpdate, updateSentence, new String[]{usuario});
//            db.close();

                intent[0] = new Intent(context, Login.class);
                startActivity(intent[0]);
                finish();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void aperturaCaja() {
        final Intent[] intent = new Intent[1];

        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_open_box, null);

        moneyOpen = dialogView.findViewById(R.id.dineroInicialInput_id);
        dineroApertura = dialogView.findViewById(R.id.dineroinicial_id);
        Button ok = dialogView.findViewById(R.id.ok_id);

        moneyOpen.addTextChangedListener(listenerMoney);

        ok.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
                configuracion.save(true, "apertura" + usuario, context);
                SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbCajaProvider.DATABASE_NAME).getWritableDatabase();
                ContentValues registerUpdate = new ContentValues();
                registerUpdate.put("ses_dinero_en_caja", dineroCaja);
                String updateSentence = "ses_usuario" + " LIKE ?" + " AND " + "ses_fh_final" + " IS NULL";
                db.update("tb_sesiones", registerUpdate, updateSentence, new String[]{usuario});
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
        dialogBuilder.setCanceledOnTouchOutside(false);
    }

    private final TextWatcher listenerMoney = new TextWatcher() {
        int dinero, resultadoCambio;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!s.toString().equals("")) {

                dinero = Integer.parseInt(s.toString());
                String sDinero = String.format("%,d", dinero);

                if (moneyClose != null) {
                    if (moneyClose.getText().hashCode() == s.hashCode()) {
                        StringBuilder sbEntregado = new StringBuilder("$");
                        sbEntregado.append(sDinero);
                        dineroCierre.setText(sbEntregado);
                        dineroCierreCaja = dinero;
                    } else {
                        resultadoCambio = dinero - resultadoAjuste;

                        String sResultadoCambio = String.format("%,d", resultadoCambio);

                        StringBuilder sbEntregado = new StringBuilder("$");
                        StringBuilder sbCambio = new StringBuilder("$");

                        sbEntregado.append(sDinero);
                        sbCambio.append(sResultadoCambio);

                        recibido.setText(sbEntregado);
                        valorPagar.setText(resultadoFinal);
                        cambio.setText(sbCambio);

                        numEntregado = dinero;
                        numCambio = resultadoCambio;
                    }
                }

                if (moneyOpen != null) {
                    if (moneyOpen.getText().hashCode() == s.hashCode()) {
                        StringBuilder sbEntregado = new StringBuilder("$");
                        sbEntregado.append(sDinero);
                        dineroApertura.setText(sbEntregado);
                        dineroCaja = dinero;
                        configuracion.save(dineroCaja, "dinero_caja" + usuario, context);
                    } else {
                        resultadoCambio = dinero - resultadoAjuste;

                        String sResultadoCambio = String.format("%,d", resultadoCambio);

                        StringBuilder sbEntregado = new StringBuilder("$");
                        StringBuilder sbCambio = new StringBuilder("$");

                        sbEntregado.append(sDinero);
                        sbCambio.append(sResultadoCambio);

                        recibido.setText(sbEntregado);
                        valorPagar.setText(resultadoFinal);
                        cambio.setText(sbCambio);

                        numEntregado = dinero;
                        numCambio = resultadoCambio;
                    }
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void enableBox() {
        box.setOnClickListener(mListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            box.setBackground(getDrawable(R.drawable.btn_round));//Default Color
        }
        box.setEnabled(true);
//        box.setTextColor(Color.parseColor("#ffffff"));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void disableBox() {
        box.setOnClickListener(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            box.setBackground(getDrawable(R.drawable.btn_round_grey));//Default Color
        }
        box.setEnabled(false);
//        box.setTextColor(Color.parseColor("#7D7D72"));
    }
}
