package com.example.chatin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.chatin.TelaLogin.EXTRA_UID;
import static com.example.chatin.TelaLogin.mAuth;
import static com.example.chatin.TelaLogin.referencia;

public class TelaChat extends AppCompatActivity {

    ValueEventListener listenerDoChat;
    private ProgressDialog dialog;
    Toolbar toolbar;
    ImageView botaoEnviar;
    TextView texto;
    String textoDoChat;
    String nomeDoUsuario;
    List<Mensagem> mensagens;
    String uidUsuario;
    int codigoDaUltimaMensagemDoBanco;
    ListView lv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_chat);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lv = findViewById(R.id.lv_chat);
        botaoEnviar = findViewById(R.id.imagem_enviar);
        texto = findViewById(R.id.txt_mensagem);

        nomeDoUsuario = mAuth.getCurrentUser().getDisplayName().split(" ")[0];
        uidUsuario = mAuth.getCurrentUser().getUid();

        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(TelaChat.this,R.color.colorPrimaryDark));

        //Inicializando variáveis

        botaoEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!texto.getText().toString().equals("")){
                    Mensagem novaMensagem = new Mensagem();
                    novaMensagem.uidRemetente = uidUsuario;
                    novaMensagem.nomeRemetente = nomeDoUsuario;
                    novaMensagem.codigo = codigoDaUltimaMensagemDoBanco+1;
                    novaMensagem.texto = texto.getText().toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    novaMensagem.data = sdf.format(new Date());
                    referencia.child("CHAT").child(String.valueOf(novaMensagem.codigo)).setValue(novaMensagem);
                    texto.setText("");
                    escondeOTeclado();
                }
            }
        });

        listenerDoChat = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                codigoDaUltimaMensagemDoBanco = 0;

                mensagens = new ArrayList<>();

                if (isOnline(TelaChat.this)){
                    try{
                        for(DataSnapshot dadosDataSnapshot: dataSnapshot.getChildren()){
                            mensagens.add(dadosDataSnapshot.getValue(Mensagem.class));
                        }
                    }catch (Exception  e){
                        //Lidar com problemas de conexão
                    }
                }else{
                    //Lidar com problemas de conexão
                }
                if(mensagens.size()>0){
                    codigoDaUltimaMensagemDoBanco = mensagens.get(mensagens.size()-1).codigo;

                    AdapterMensagens adapterMensagem = new AdapterMensagens(uidUsuario,mensagens, TelaChat.this);

                    lv.setAdapter(adapterMensagem);

                    lv.setSelection(mensagens.size()-1);
                }

                if(dialog.isShowing()){
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        dialog = ProgressDialog.show(TelaChat.this,"","Carregando dados...",true,false);

        referencia.child("CHAT").addValueEventListener(listenerDoChat);
    }

    @Override
    protected void onPause() {
        super.onPause();
        referencia.child("CHAT").removeEventListener(listenerDoChat);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_logout){
            verificaLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        Context context = TelaChat.this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(80,30,80,0);

        TextView textoAlerta = new TextView(TelaChat.this);
        //textoAlerta.setTypeface(ResourcesCompat.getFont(TelaLogin.this, R.font.cabin));
        textoAlerta.setText("Deseja sair do App?");
        textoAlerta.setTextSize(17);

        AlertDialog.Builder builder = new AlertDialog.Builder(TelaChat.this, R.style.AlertDialogCustom);

        layout.addView(textoAlerta);
        builder.setView(layout);

        //Define o título do diálogo
        builder.setTitle("Aviso");
        //builder.setIcon(R.drawable.ic_logout_verde);

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });

        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alerta = builder.create();
        alerta.show();
    }

    public void escondeOTeclado(){
        View view = this.getCurrentFocus();
        if(view!=null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void verificaLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);

        Context context = TelaChat.this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(80,30,80,0);

        TextView textoAlerta = new TextView(TelaChat.this);
        //textoAlerta.setTypeface(ResourcesCompat.getFont(this, R.font.cabin));
        textoAlerta.setText("Deseja desconectar da sua conta?");
        textoAlerta.setTextSize(17);

        //Define o título do diálogo
        builder.setTitle("Logoff");
        //builder.setIcon(R.drawable.ic_logout_verde);

        layout.addView(textoAlerta);
        builder.setView(layout);

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if(isOnline(TelaChat.this)){
                    try{
                        //Este metodo desconecta a conta google do usuário do fire base
                        revokeAccess();
                        FirebaseAuth.getInstance().signOut();
                        Intent it  = new Intent(TelaChat.this, TelaLogin.class);
                        startActivity(it);
                        finish();
                    }catch (Exception e){
                        //Lidar com erro de conexao
                    }
                }else{
                    //Lidar com erro de conexao
                }
            }
        });

        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alerta = builder.create();
        alerta.show();
    }

    private boolean revokeAccess() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
        return true;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager administradorDeConexao = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo informacoesDeConexao = administradorDeConexao.getActiveNetworkInfo();
        if (informacoesDeConexao != null && informacoesDeConexao.isConnected())
            return true;
        else
            return false;
    }
}
