package com.example.chatin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class AdapterMensagens extends BaseAdapter {
    List<Mensagem> lista;
    Context context;
    String uidUsuario;
    DecimalFormat df = new DecimalFormat("#,###.00");

    public AdapterMensagens(String uidUsuario,List<Mensagem> lista, Context context){
        this.uidUsuario = uidUsuario;
        this.lista = lista;
        this.context = context;
    }
    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView nome;
        TextView texto;
        TextView data;

        if(lista.get(position).uidRemetente.equals(uidUsuario)){

            view = mInflater.inflate(R.layout.layout_lista_de_mensagens_pessoal, null);

            // Atribuição normal dos campos de uma view
            nome = view.findViewById(R.id.tv_nome_pessoal);
            texto = view.findViewById(R.id.tv_texto_pessoal);
            data = view.findViewById(R.id.tv_data_pessoal);

            nome.setText("Você");
        }else{
            // Cria uma view com o layout  do seu item
            view = mInflater.inflate(R.layout.layout_lista_de_mensagens, null);

            // Atribuição normal dos campos de uma view
            nome = view.findViewById(R.id.tv_nome);
            texto = view.findViewById(R.id.tv_texto);
            data = view.findViewById(R.id.tv_data);

            nome.setText(lista.get(position).nomeRemetente);

        }

        texto.setText(lista.get(position).texto);
        data.setText(lista.get(position).data);

        return view;
    }
}
