package com.example.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.uber.R;
import com.example.uber.config.ConfiguracaoFirebase;
import com.example.uber.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

public class CadastroActivity extends AppCompatActivity {

  private TextInputEditText campoNome, campoEmail, campoSenha;
  private Switch switchTipoUsuario;
  private FirebaseAuth autenticacao;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cadastro);

    //Inicializar os componentes
    campoNome = findViewById(R.id.editCadastroNome);
    campoEmail = findViewById(R.id.editCadastroEmail);
    campoSenha = findViewById(R.id.editCadastroSenha);
    switchTipoUsuario = findViewById(R.id.switchTipoUsuario);
  }

  public void validarCadastroUsuario(View view) {

    //Recuperar textos dos campos
    String textoNome = campoNome.getText().toString();
    String textoEmail = campoEmail.getText().toString();
    String textoSenha = campoSenha.getText().toString();

    if (!textoNome.isEmpty()) {
      if (!textoEmail.isEmpty()) {
        if (!textoSenha.isEmpty()) {

          Usuario usuario = new Usuario();
          usuario.setNome(textoNome);
          usuario.setEmail(textoEmail);
          usuario.setSenha(textoSenha);
          usuario.setTipo(verificaTipoUsuario());

          cadastrarUsuario(usuario);

        } else {
          Toast.makeText(CadastroActivity.this,
                  "Preencha a senha!",
                  Toast.LENGTH_SHORT).show();
        }
      } else {
        Toast.makeText(CadastroActivity.this,
                "Preencha o email!",
                Toast.LENGTH_SHORT).show();
      }
    } else {
      Toast.makeText(CadastroActivity.this,
              "Preencha o nome!",
              Toast.LENGTH_SHORT).show();
    }
  }

  public String verificaTipoUsuario() {
    return switchTipoUsuario.isChecked() ? "M" : "P";
  }

  public void cadastrarUsuario(Usuario usuario) {
    autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    autenticacao.createUserWithEmailAndPassword(
            usuario.getEmail(),
            usuario.getSenha()
    ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
      @Override
      public void onComplete(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
          Toast.makeText(CadastroActivity.this,
                  "Sucesso ao cadastrar o usuário!",
                  Toast.LENGTH_SHORT).show();
        }
      }
    });
  }
}
