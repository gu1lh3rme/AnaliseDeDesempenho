/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulacao;

import java.util.ArrayList;

/**
 *
 * @author 2014.1.08.012
 */
public class Conexao {
    double duracao_conexao;
    //double qtd_pcts_conexao;
    double chegada_proximo_pct_cbr;
    double tempoAtual;  //Tempo em que a conexão começou
    double tam;    
    ArrayList<Pacote> filaCbr = new ArrayList();

    public Conexao(double chegada_proximo_pct_cbr, double qtd_pcts_conexao, double duracao_conexao, double tam, double tempoAtual) {
        this.chegada_proximo_pct_cbr = chegada_proximo_pct_cbr;
        //Recebo o tempo de chegada do próximo pct cbr e incremento 0.02
        for (int i = 0; i < qtd_pcts_conexao; i++) {
            filaCbr.add(new Pacote(tam, this.chegada_proximo_pct_cbr));
            this.chegada_proximo_pct_cbr += chegada_proximo_pct_cbr; 
        }
    //    this.qtd_pcts_conexao = qtd_pcts_conexao;
        this.duracao_conexao = duracao_conexao;
        this.tempoAtual = tempoAtual;
    }   
}
