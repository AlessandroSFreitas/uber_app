package com.example.uber.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uber.R
import com.example.uber.config.ConfiguracaoFirebase
import com.example.uber.helper.UsuarioFirebase
import com.example.uber.model.Usuario
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var campoEmail: TextInputEditText
    private lateinit var campoSenha: TextInputEditText
    private lateinit var autenticacao: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Inicializar componentes
        inicializarComponentes()

    }

    fun validarLoginUsuario(view: View) {

        //Recuperar textos dos campos
        val textoEmail = campoEmail.text.toString()
        val textoSenha = campoSenha.text.toString()
        val usuario = Usuario()

        if ( !textoEmail.isEmpty() ) {

            if ( !textoSenha.isEmpty() ) {

                usuario.senha = textoSenha
                usuario.email = textoEmail

                logarUsuario(usuario)

            } else {

                Toast.makeText(this@LoginActivity,
                        "Preencha a senha!",
                        Toast.LENGTH_SHORT).show()

            }

        } else {

            Toast.makeText(this@LoginActivity,
                    "Preencha o email!",
                    Toast.LENGTH_SHORT).show()

        }

    }

    fun logarUsuario(usuario: Usuario) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao()

        autenticacao.signInWithEmailAndPassword(usuario.email, usuario.senha)
                .addOnCompleteListener { task: Task<AuthResult> ->

                    if ( task.isSuccessful ) {

                        //Verificar o tipo de usuário logado
                        // "Motorista" / "Passageiro"
                        UsuarioFirebase.redirecionaUsuarioLogado(this@LoginActivity)

                    } else {

                        var excecao: String? = ""

                        try {

                            throw task.exception!!

                        } catch ( e: FirebaseAuthInvalidUserException ) {

                            excecao = "Usuário não está cadastrado!"

                        } catch ( e: FirebaseAuthInvalidCredentialsException ) {

                            excecao = "E-mail e senha não correspondem a um usuário cadastrado!"

                        } catch ( e: Exception ) {

                            excecao = "Erro ao cadastrar usuário: " + e.message
                            e.printStackTrace()

                        }

                        Toast.makeText(this@LoginActivity,
                                excecao, Toast.LENGTH_SHORT).show()

                    }

                }

    }

    fun inicializarComponentes() {

        campoEmail = findViewById(R.id.editLoginEmail)
        campoSenha = findViewById(R.id.editLoginSenha)

    }

}