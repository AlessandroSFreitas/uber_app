package com.example.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.uber.config.ConfiguracaoFirebase;
import com.example.uber.helper.UsuarioFirebase;
import com.example.uber.model.Requisicao;
import com.example.uber.model.Usuario;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.Button;

import com.example.uber.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CorridaActivity extends AppCompatActivity implements OnMapReadyCallback {

  private GoogleMap mMap;
  private LocationManager locationManager;
  private LocationListener locationListener;
  private LatLng localMotorista;
  private Usuario motorista;
  private String idRequisicao;
  private Requisicao requisicao;
  private DatabaseReference firebaseRef;
  private Button buttonAceitarCorrida;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_corrida);

    inicializarComponentes();

    // Recupera dados do usuário
    if (getIntent().getExtras().containsKey("idRequisicao") && getIntent().getExtras().containsKey("motorista")) {
      Bundle extras = getIntent().getExtras();
      motorista = (Usuario) extras.getSerializable("motorista");
      idRequisicao = extras.getString("idRequisicao");

      vericaStatusRequisicao();
    }
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    // Recuperar a localização do usuário
    recuperarLocalizacaoUsuario();
  }

  public void aceitarCorrida(View view) {
    // Configurar requisição
    requisicao = new Requisicao();
    requisicao.setId(idRequisicao);
    requisicao.setMotorista(motorista);
    requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);

    requisicao.atualizar();
  }

  private void vericaStatusRequisicao() {
    DatabaseReference requisicoes = firebaseRef.child("requisicoes")
        .child(idRequisicao);
    requisicoes.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        // Recuperar requisição
        requisicao = dataSnapshot.getValue(Requisicao.class);
        switch (requisicao.getStatus()) {
          case Requisicao.STATUS_AGUARDANDO :
            requisicaoAguardando();
            break;
          case Requisicao.STATUS_A_CAMINHO :
            requisicaoACaminho();
            break;
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });
  }

  private void requisicaoAguardando() {
    buttonAceitarCorrida.setText("Aceitar corrida");
  }

  private void requisicaoACaminho() {
    buttonAceitarCorrida.setText("A caminho do passageiro");
  }

  private void recuperarLocalizacaoUsuario() {
    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    locationListener = new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        //recuperar latitude e longitude
        double latitute = location.getLatitude();
        double longitude = location.getLongitude();

        localMotorista = new LatLng(latitute, longitude);

        mMap.clear();
        mMap.addMarker(
            new MarkerOptions()
                .position(localMotorista)
                .title("Meu local")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localMotorista, 15));
      }

      @Override
      public void onStatusChanged(String provider, int status, Bundle extras) {

      }

      @Override
      public void onProviderEnabled(String provider) {

      }

      @Override
      public void onProviderDisabled(String provider) {

      }
    };

    //Solicitar atualizações de localização
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
      locationManager.requestLocationUpdates(
          LocationManager.GPS_PROVIDER,
          5000,
          10,
          locationListener
      );
    }
  }

  private void inicializarComponentes() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle("Iniciar corrida");

    buttonAceitarCorrida = findViewById(R.id.buttonAceitarCorrida);

    //Configuracoes iniciais
    firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }
}
