package com.miraimx.kinderscontrol;

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
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.miraimx.kinderscontrol.databinding.ActivityLeerQrBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LeerQR_Activity extends AppCompatActivity {
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    cargarDatos(result.getContents());
                    cargarDatosTutor(result.getContents());
                }
            });
    String idEmpleado, idTutor, estatusRegistro;
    //idTutor = "8PalsQD1XmMSEELuEh8x8maxqdv2",
    private final List<Usuario> alumnoLista = new ArrayList<>();
    //private RecyclerAdapter2 recyclerAdapterAlumnos;
    private ArrayAdapter listViewAdapter;

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

        binding.registrarIn.setEnabled(false);
        TextView txtTitulo = findViewById(R.id.tvRegistroActivity);


        if (estatusRegistro.equals("in")) {
            binding.registrarIn.setText("Registrar entrada");
            txtTitulo.setText("Registro de entrada");
        } else {
            binding.registrarIn.setText("Registrar salida");
            txtTitulo.setText("Registro de salida");
        }

        binding.registrarIn.setOnClickListener(view -> btnRegistrarEntrada());

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
            binding.registrarIn.setEnabled(true);
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
                cargarDatos(idTutor);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void cargarDatosTutor(String cUserId) {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("tutores").orderByChild("tutor_id").equalTo(cUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot datos : snapshot.getChildren()) {
                    String nombreTutor = datos.child("nombre_tutor").getValue(String.class);
                    String telefono = datos.child("telefono_tutor").getValue(String.class);
                    String correo = datos.child("correo_tutor").getValue(String.class);
                    String direccion = datos.child("direccion_tutor").getValue(String.class);
                    if (nombreTutor != null && telefono != null && correo != null && direccion != null) {
                        binding.txtNombraTutorQR.setText(nombreTutor);
                        binding.txtTelefonoTutorQR.setText(telefono);
                        binding.txEmailTutorQR.setText(correo);
                        binding.txDireccionTutorQR.setText(direccion);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void cargarDatos(String cUserId) {
        alumnoLista.clear();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query query = database.child("tutorizacion").orderByChild("tutor_id").equalTo(cUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final int[] cantidadAlumnos = {0};

                for (DataSnapshot registro : snapshot.getChildren()) {
                    final String id = registro.child("matricula").getValue(String.class);

                    if (id != null) {
                        Query queryAlumno = database.child("alumnos").orderByChild("matricula").equalTo(id);

                        queryAlumno.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot alumno : snapshot.getChildren()) {
                                    String nombreAlumno = alumno.child("nombre_alumno").getValue(String.class);

                                    if (nombreAlumno != null) {
                                        Usuario usuarioDatos = new Usuario(id, nombreAlumno, false, "");
                                        alumnoLista.add(usuarioDatos);
                                    }
                                }

                                cantidadAlumnos[0]++;

                                if (cantidadAlumnos[0] == snapshot.getChildrenCount()) {
                                    // Notificar al adaptador sobre los cambios
                                    listViewAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Manejar la cancelación si es necesario
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar la cancelación si es necesario
            }
        });
    }

    private void btnRegistrarEntrada() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        List<String> alumnoSeleccion = new ArrayList<>();

        builder.setTitle("Confirmación");
        builder.setMessage("¿Desea realizar la asignación de los alumnos?");

        builder.setPositiveButton("Sí", (dialog, which) -> {
            // Aquí puedes agregar la lógica para agregar al niño al tutor
            //Toast.makeText(LeerQR_Activity.this, "Registro de " + estatusRegistro + " exitoso", Toast.LENGTH_SHORT).show();

            DatabaseReference chekinRef = FirebaseDatabase.getInstance().getReference("checkin").push();
            HashMap<String, Object> checkInfo = new HashMap<>();

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String fechaHoraActual = dateFormat.format(calendar.getTime());

            int y = 0;

            for (Usuario alumno : alumnoLista) {
                if (alumno.getSeleccionado()) {
                    alumnoSeleccion.add(alumno.getId());
                }
            }

            while (y < alumnoSeleccion.size()) {
                checkInfo.put("matricula", alumnoSeleccion.get(y));
                checkInfo.put("tutor_id", idTutor);
                checkInfo.put("empleado_id", idEmpleado);
                checkInfo.put("in_out", estatusRegistro);
                checkInfo.put("horafecha_check", fechaHoraActual);
                y++;
            }

            chekinRef.setValue(checkInfo).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LeerQR_Activity.this, "Operación exitosa", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LeerQR_Activity.this, "No se pudo realizar la operación", Toast.LENGTH_SHORT).show();
                }
            });

            while (y < alumnoSeleccion.size()) {
                alumnoLista.get(y).setSeleccionado(false);
            }


            Intent intent = new Intent(this, PanelAdmin.class);
            startActivity(intent);
            finish();

            /*alumnoLista.get(posAnteriorAlumno).setSeleccionado(false);
            posAnteriorAlumno = -1;
            binding.lsCheckAlumno.clearChoices();
            listViewAdapter.notifyDataSetChanged();
            binding.registrarIn.setEnabled(false);
            dialog.dismiss();*/
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            // Aquí puedes agregar la lógica si el usuario elige "No"
            // Toast.makeText(LeerQR_Activity.this, "No registro la entrada del niño", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}