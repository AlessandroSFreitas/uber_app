package com.example.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.uber.R;
import com.example.uber.config.ConfiguracaoFirebase;
import com.example.uber.helper.Permissoes;
import com.example.uber.helper.UsuarioFirebase;
import com.google.firebase.auth.FirebaseAuth;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

//  private FirebaseAuth autenticacao;
  private String[] permissoes = new String[] {
          Manifest.permission.ACCESS_FINE_LOCATION
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportActionBar().hide();

    //Validar as permissões
    Permissoes.validarPermissoes(permissoes, this, 1);

  }

  public void abrirTelaLogin(View view) {
    startActivity( new Intent(this, LoginActivityKt.class));
  }

  public void abrirTelaCadastro(View view) {
    startActivity( new Intent(this, CadastroActivity.class));
  }

  @Override
  protected void onStart() {

    super.onStart();
    UsuarioFirebase.redirecionaUsuarioLogado(MainActivity.this);

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    for (int permissaoResultado : grantResults) {
      if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
        alertaValidacaoPermissao();
      }
    }
  }

  private void alertaValidacaoPermissao() {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Permissões negadas");
    builder.setMessage("Para utilizar o app é necessário aceitas as permissões");
    builder.setCancelable(false);
    builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        finish();
      }
    });

    AlertDialog dialog = builder.create();
    dialog.show();

  }
}
