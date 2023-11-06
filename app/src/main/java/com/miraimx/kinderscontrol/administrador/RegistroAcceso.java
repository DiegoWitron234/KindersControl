package com.miraimx.kinderscontrol.administrador;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.miraimx.kinderscontrol.ControlFirebaseBD;
import com.miraimx.kinderscontrol.DatosConsultados;
import com.miraimx.kinderscontrol.ListViewUsuarioAdapter;
import com.miraimx.kinderscontrol.R;
import com.miraimx.kinderscontrol.Usuario;
import com.miraimx.kinderscontrol.databinding.ActivityLeerQrBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegistroAcceso extends AppCompatActivity {
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    cargarDatosTutor(result.getContents());
                }
            });
    String idEmpleado, idTutor, estatusRegistro;
    //idTutor = "8PalsQD1XmMSEELuEh8x8maxqdv2",
    private final List<Usuario> alumnoLista = new ArrayList<>();
    //private RecyclerAdapter2 recyclerAdapterAlumnos;
    private ArrayAdapter listViewAdapter;
    private final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private ActivityLeerQrBinding binding;

    private int posAnteriorAlumno = -1;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeerQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        modoOscuro();
        MobileAds.initialize(this, initializationStatus -> {
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);

        idEmpleado = getIntent().getStringExtra("id");
        estatusRegistro = getIntent().getStringExtra("estatus");

        binding.registrar.setEnabled(false);
        TextView txtTitulo = findViewById(R.id.tvRegistroActivity);


        if (estatusRegistro.equals("in")) {
            binding.registrar.setText("Registrar entrada");
            txtTitulo.setText("Registro de entrada");
        } else {
            binding.registrar.setText("Registrar salida");
            txtTitulo.setText("Registro de salida");
        }

        binding.registrar.setOnClickListener(view -> btnRegistrarEntrada());

        binding.btnScan.setOnClickListener(view -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Leer código QR");
            options.setCameraId(0);  // Use a specific camera of the device
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(true);
            barcodeLauncher.launch(options);
        });

        listViewAdapter = new ListViewUsuarioAdapter(this, alumnoLista);
        binding.lsCheckAlumno.setAdapter(listViewAdapter);
        binding.lsCheckAlumno.setOnItemClickListener((adapterView, view, i, l) -> {
            Usuario elementoSeleccionado = alumnoLista.get(i);
            elementoSeleccionado.setSeleccionado(true);
            if (posAnteriorAlumno != -1 && posAnteriorAlumno != i) {
                alumnoLista.get(posAnteriorAlumno).setSeleccionado(false);
            }
            posAnteriorAlumno = i;
            binding.registrar.setEnabled(true);
        });
    }

    private void modoOscuro() {
        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {/* si está activo el modo oscuro lo desactiva */
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Lectura cancelada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_SHORT).show();
                idTutor = result.getContents();
                //cargarDatos(idTutor);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void cargarDatosTutor(String cUserId) {
        Query query = database.child("tutores").orderByChild("tutor_id").equalTo(cUserId);
        ControlFirebaseBD controlFirebaseBD = new ControlFirebaseBD(new DatosConsultados() {
            @Override
            public void onDatosConsulta(@NonNull List<String> resultados) {
                super.onDatosConsulta(resultados);
                binding.txtNombraTutorQR.setText(resultados.get(0));
                binding.txtTelefonoTutorQR.setText(resultados.get(1));
                binding.txEmailTutorQR.setText(resultados.get(2));
                binding.txDireccionTutorQR.setText(resultados.get(3));
                cargarDatos(cUserId, resultados.get(0));
            }
        });
        String[] atributos = {"nombre_tutor", "telefono_tutor", "correo_tutor", "direccion_tutor"};
        controlFirebaseBD.consultar(query, atributos);
    }

    private void cargarDatos(String cUserId, String nombreUsuario) {
        ControlFirebaseBD controlFirebaseBD = new ControlFirebaseBD(new DatosConsultados() {
            @Override
            public void onDatosUsuario(@NonNull List<Usuario> resultados) {
                super.onDatosUsuario(resultados);
                listViewAdapter.notifyDataSetChanged();
            }
        });
        controlFirebaseBD.consultaTutorizaciones(cUserId, nombreUsuario, alumnoLista);
    }

    private void btnRegistrarEntrada() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmación");
        builder.setMessage("¿Desea realizar la asignación de los alumnos?");

        builder.setPositiveButton("Sí", (dialog, which) -> {
            // Aquí puedes agregar la lógica para agregar al niño al tutor
            HashMap<String, Object> checkInfo = new HashMap<>();
            String alumnoSeleccion = "";
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss", Locale.getDefault());
            String[] fechaHoraActual = dateFormat.format(calendar.getTime()).split(",");
            for (Usuario alumno : alumnoLista) {
                if (alumno.getSeleccionado()) {
                    alumnoSeleccion = alumno.getId();
                    break;
                }
            }

            checkInfo.put("profesor_id", idEmpleado);
            checkInfo.put("matricula", alumnoSeleccion);
            checkInfo.put("tutor_id", idTutor);
            checkInfo.put("estatus", estatusRegistro);
            checkInfo.put("fecha_acceso", fechaHoraActual[0]);
            checkInfo.put("hora_acceso", fechaHoraActual[1]);

            // Cargando Datos
            // Se cambió de checkin a acceso
            database.child("acceso").push().setValue(checkInfo).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(RegistroAcceso.this, "Operación exitosa", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegistroAcceso.this, "No se pudo realizar la operación", Toast.LENGTH_SHORT).show();
                }
            });
            //Actualizar Datos
            DatabaseReference alumnoRef = database.child("alumnos/" + alumnoSeleccion + "/acceso");
            HashMap<String, Object> acceso = new HashMap<>();
            acceso.put("estatus", estatusRegistro);
            acceso.put("fecha_acceso", fechaHoraActual[0]);
            acceso.put("hora_acceso", fechaHoraActual[1]);
            alumnoRef.updateChildren(acceso);

            alumnoLista.get(0).setSeleccionado(false);
            Intent intent = new Intent(this, PanelAdmin.class);
            startActivity(intent);
            finish();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            // Aquí puedes agregar la lógica si el usuario elige "No"
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}