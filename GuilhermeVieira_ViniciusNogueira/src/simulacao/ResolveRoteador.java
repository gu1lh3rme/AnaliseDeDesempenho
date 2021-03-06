/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulacao;

import static java.lang.Math.log;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author 2014.1.08.012
 */
public class ResolveRoteador {

    private double tam_pct;
    private double saida_pct_atendimento;
    private double ocupacao, ocupacao_web = 0.0, ocupacao_cbr = 0.0;
    private double link;
    private Little en_cbr, ew_chegada_cbr, ew_saida_cbr;
    private Little en_web, ew_chegada_web, ew_saida_web;

    //Numero aleatorio entre 0 e 1
    public ResolveRoteador() {
    }

    private Random gerador;

    public double aleatorio() {
        double u = gerador.nextDouble();    //Retorna um número entre 0 e 1
        //resultado sera algo entre [0,0.999999...] proximo a 1.0
        return u;
    }

    /**
     *
     * @param l
     * @return intervalo de tempo, com media tendendo ao intervalo informado
     * pelo usuário
     */
    public double chegada_pct(double l) {
        return ((-1.0 / l)) * log(aleatorio());
    }

    /**
     *
     * @param parametro_l da exponencial
     * @return intervalo de tempo, com media tendendo ao intervalo informado
     * pelo usuário
     */
    public double gera_tam_pct() {
        double a = aleatorio();
        //tamanhos convertidos para Mb
        if (a <= 0.5) {
            return (550.0 * 8.0) / (1000000.0);
        } else if (a <= 0.9) {
            return (40.0 * 8.0) / (1000000.0);
        } else {
            return (1500.0 * 8.0) / (1000000.0);
        }
    }

    /**
     * @param a
     * @param b
     * @return menor dentre os valores
     */
    public double minimo(double a, double b) {
        if (a <= b) {
            return a;
        }
        return b;
    }

    private Pacote proxPacote(List<Conexao> conexoes) {
        Pacote pct = new Pacote(null, null);
        Conexao c = new Conexao(0.0, 0.0, 0.0, 0.0, 0);
        double menorTempo = Double.MAX_VALUE;
        int posicao = -1;

        for (int i = 0; i < conexoes.size(); i++) {
            c = conexoes.get(i);
            if (c.filaCbr.get(0).tempo < menorTempo) { //Tempo da conexao que começou primeiro
                pct = c.filaCbr.get(0); //Retornar removendo
                menorTempo = c.filaCbr.get(0).tempo;
                posicao = i;
            }
        }

        //Remove o pct da fila da conexao depois de pegar esse pct
        //que vai para a fila do roteador
        if (posicao >= 0) {     //Existe uma conexao cbr
            conexoes.get(posicao).filaCbr.remove(0);
            //Verifica se a fila da conexão ficou vazia e remove ela da lista de conexões
            if (conexoes.get(posicao).filaCbr.isEmpty()) {
                conexoes.remove(posicao);
            }
        }
        return pct;
    }

    public double ocupacao_pct_Web(double tam, double link, double intervalo) {
        return tam * intervalo / link;
    }

    public double ocupacao_pct_Cbr(double tam, double link, double chegada_proximo_pct_cbr, double duracao, double intervalo_conexao_cbr) {
        return (duracao * tam) / link * chegada_proximo_pct_cbr * intervalo_conexao_cbr;
    }

    double intervaloPacote(double link, double ocupacao, double tamMedioPacote) {
        double result = (link * ocupacao) / tamMedioPacote;
        return (1.0 / result);
    }

    public double gera_intervalo_cbr(double minimo, double maximo) {
        Random random = new Random();
        return random.nextDouble() * (maximo - minimo) + minimo;
    }

    private void saida_de_pacote(ArrayList<Pacote> fila, double tempo, boolean isweb) {
        if (!fila.isEmpty()) {
            tam_pct = fila.get(0).tamanho;
            //gerando o tempo em que o pacote atual sairá do sistema
            saida_pct_atendimento = tempo + tam_pct / link;

            ocupacao += saida_pct_atendimento - tempo;
            if (isweb) {
                ocupacao_web += saida_pct_atendimento - tempo;
            } else {
                ocupacao_cbr += saida_pct_atendimento - tempo;
            }
        }
    }

    public void Resolve() {
        Pacote inicio;
        ArrayList<Conexao> filaConexoes = new ArrayList();
        //variavel para en
        this.en_cbr = new Little(0.0, 0.0, 0.0);
        //variável para ew chegada
        this.ew_chegada_cbr = new Little(0.0, 0.0, 0.0);
        //variável para ew saída
        this.ew_saida_cbr = new Little(0.0, 0.0, 0.0);

        this.en_web = new Little(0.0, 0.0, 0.0);
        //variável para ew chegada
        this.ew_chegada_web = new Little(0.0, 0.0, 0.0);
        //variável para ew saída
        this.ew_saida_web = new Little(0.0, 0.0, 0.0);

        //iniciando a semente para a geração
        //dos números pseudoaleatorios
        int semente = 1556915527;
        System.out.println("Semente: " + semente);
        gerador = new Random(semente);
        //tempo atual
        double tempo = 0.0;
        //tempo total
        double tempo_total = 10000.0;

        //intervalo médio entre pacotes 
        double intervalo = intervaloPacote(1250000, 0.6, 441);  //0.000441 -> para 0.8
        //double intervalo = 0.001176;  //0.000441
        System.out.printf("Intervalo web: %.6f\n", intervalo);
        //ajustando parametro para a exponencial
        intervalo = 1.0 / intervalo;    //2267,573696145

        //Tam pacote gerado
        tam_pct = 0.0;

        //Tamanho do link do roteador 1250000
        link = 10.0;

        //tempo de chegada do proximo pacote
        //ao sistema
        double chegada_proximo_pct = chegada_pct(intervalo);
        //System.out.println("Chegada do primeiro pacote: %lF\n", chegada_proximo_pct);
        double chegada_proximo_pct_cbr = gera_intervalo_cbr(0.01, 0.02);
        //Define a taxa de chegada da próxima conexao, duração e qtd pcts da conexão
        // double duracao_conexao = duracao((1200.0 * 8.0) / (1000000.0), link, chegada_proximo_pct_cbr, 0.3, chegada_pct(0.3));
        double duracao_conexao = 12;
        double chegada_proxima_conexao = chegada_proximo_pct_cbr;  //Primeira conexão começa quando o primeiro cbr chega
        // double intervalo_conexao_cbr = chegada_pct(0.3);
        double qtd_pcts_conexao = duracao_conexao / chegada_proximo_pct_cbr;
        double intervalo_cbr = chegada_proximo_pct_cbr;
        saida_pct_atendimento = 0.0;
        ocupacao = 0.0;
        int qtd_total_conexoes = 0;

        //30% web  3528*qtd_pcts = 3000000   -> 30% do link
        //3528 -> 441 bytes em bits
        double ocweb = ocupacao_pct_Web(441, 1250000, intervalo);
//        double ocCbr = ocupacao_pct_Cbr(1200, 1250000, chegada_proximo_pct_cbr, duracao_conexao);
        double ocCbr = ocupacao_pct_Cbr((1200.0 * 8.0) / (1000000.0), 10, chegada_proximo_pct_cbr, tempo_total, chegada_pct(0.3));
        System.out.println("Ocupacao Web (fórmula): " + ocweb);
        System.out.println("Ocupacao CBR (fórmula): " + ocCbr);
        System.out.println("Ocupação total: " + (ocweb + ocCbr));
        System.out.println("\n#####################Simulação#######################");

        ArrayList<Pacote> filaCbr = new ArrayList();
        ArrayList<Pacote> filaWeb = new ArrayList();

        while (tempo <= tempo_total) {
            //roteador vazio. Logo avanço no tempo de chegada do
            //proximo pacote
            if (filaWeb.isEmpty() && filaCbr.isEmpty()) {
                tempo = minimo(chegada_proximo_pct, chegada_proximo_pct_cbr);
            } else {
                //Há fila!
                tempo = minimo(minimo(minimo(chegada_proximo_pct_cbr, chegada_proximo_pct), saida_pct_atendimento), chegada_proxima_conexao);
            }

            if (tempo == chegada_proxima_conexao) {
                qtd_total_conexoes++;
                //Se a fila de conexoes não estiver vazia gera o intervalo da nova conexão
                if (!filaConexoes.isEmpty()) {
                    intervalo_cbr = gera_intervalo_cbr(0.01, 0.02);
                    qtd_pcts_conexao = (duracao_conexao / intervalo_cbr);
                }
                chegada_proxima_conexao = tempo + chegada_pct(0.3);
                tam_pct = (1200.0 * 8.0) / (1000000.0);
                filaConexoes.add(new Conexao(chegada_proximo_pct_cbr, qtd_pcts_conexao, duracao_conexao, tam_pct, tempo));
            }

            //chegada de pacote
            if (tempo == chegada_proximo_pct) {
                //roteador estava livre
                tam_pct = gera_tam_pct();

                if (filaWeb.isEmpty()) {

                    //descobrir o tamanho do pacote
                    //gerando o tempo em que o pacote atual sairá do sistema
                    saida_pct_atendimento = tempo + tam_pct / link;

                    ocupacao += saida_pct_atendimento - tempo;
                    ocupacao_web += saida_pct_atendimento - tempo;
                }
                //pacote colocado na fila
                inicio = new Pacote(tam_pct, tempo);
                filaWeb.add(inicio);
                //gerar o tempo de chegada do próximo
                chegada_proximo_pct = tempo + chegada_pct(intervalo);

                //cálculo little -- E[N]
                en_web.soma_areas += en_web.qtd_pacotes * (tempo - en_web.tempo_anterior);
                en_web.qtd_pacotes++;
                en_web.tempo_anterior = tempo;

                ew_chegada_web.soma_areas += ew_chegada_web.qtd_pacotes * (tempo - ew_chegada_web.tempo_anterior);
                ew_chegada_web.qtd_pacotes++;
                ew_chegada_web.tempo_anterior = tempo;

                //atualizar próxima conexão
            } else if (tempo == chegada_proximo_pct_cbr) {
                //chega pct cbr 1200 bytes
                tam_pct = (1200.0 * 8.0) / (1000000.0);

                if (filaCbr.isEmpty()) {
                    //descobrir o tamanho do pacote
                    //gerando o tempo em que o pacote atual sairá do sistema
                    saida_pct_atendimento = tempo + tam_pct / link;

                    ocupacao += saida_pct_atendimento - tempo;
                    ocupacao_cbr += saida_pct_atendimento - tempo;
                }

                //pacote colocado na fila
                inicio = proxPacote(filaConexoes);

                filaCbr.add(inicio);
                //gerar o tempo de chegada do próximo
                chegada_proximo_pct_cbr += intervalo_cbr;

                //cálculo little  --- E[N]
                en_cbr.soma_areas += en_cbr.qtd_pacotes * (tempo - en_cbr.tempo_anterior);
                en_cbr.qtd_pacotes++;
                en_cbr.tempo_anterior = tempo;

                //cálculo little  --- E[W] chegada
                ew_chegada_cbr.soma_areas += ew_chegada_cbr.qtd_pacotes * (tempo - ew_chegada_cbr.tempo_anterior);
                ew_chegada_cbr.qtd_pacotes++;
                ew_chegada_cbr.tempo_anterior = tempo;

            } else { //saida de pacote
                //Remover da fila na proporção
                //Se a fila web tiver vazia remove da cbr
                boolean saida_web = false;
                if (filaWeb.isEmpty()) {
                    filaCbr.remove(0);
                    saida_de_pacote(filaCbr, tempo, saida_web);
                } else if (filaCbr.isEmpty()) { //Se a fila cbr tiver vazia remove da web
                    filaWeb.remove(0);
                    saida_web = true;
                    saida_de_pacote(filaWeb, tempo, saida_web);
                } else {  //Se as duas filas não tiverem vazias verifica a proporção de atraso
                    Pacote web, cbr;
                    web = filaWeb.get(0);
                    cbr = filaCbr.get(0);
                    if (cbr.tempo == null) {
                        filaWeb.remove(0);
                    } else {
                        double atrasoWeb = tempo - web.tempo;
                        double atrasoCbr = tempo - cbr.tempo;
                        if (atrasoCbr <= atrasoWeb * 0.5) { //Se o atraso cbr for metade do atraso web remove o cbr
                            filaCbr.remove(0);
                            saida_de_pacote(filaCbr, tempo, saida_web);
                        } else { //Senão o atraso cbr é maior que a metade do atraso web
                            filaWeb.remove(0);
                            saida_web = true;
                            saida_de_pacote(filaWeb, tempo, saida_web);
                        }
                    }
                }

                if (saida_web) {
                    //cálculo little -- E[N]
                    en_web.soma_areas += en_web.qtd_pacotes * (tempo - en_web.tempo_anterior);
                    en_web.qtd_pacotes--;
                    en_web.tempo_anterior = tempo;

                    //cálculo little  --- E[W] saída
                    ew_saida_web.soma_areas += ew_saida_web.qtd_pacotes * (tempo - ew_saida_web.tempo_anterior);
                    ew_saida_web.qtd_pacotes++;
                    ew_saida_web.tempo_anterior = tempo;
                } else {
                    //cálculo little -- E[N]
                    en_cbr.soma_areas += en_cbr.qtd_pacotes * (tempo - en_cbr.tempo_anterior);
                    en_cbr.qtd_pacotes--;
                    en_cbr.tempo_anterior = tempo;

                    //cálculo little  --- E[W] saída
                    ew_saida_cbr.soma_areas += ew_saida_web.qtd_pacotes * (tempo - ew_saida_cbr.tempo_anterior);
                    ew_saida_cbr.qtd_pacotes++;
                    ew_saida_cbr.tempo_anterior = tempo;
                }
            }
        }

        System.out.printf("Quantidades pacotes total: %.0f\n", ew_chegada_cbr.qtd_pacotes + ew_chegada_web.qtd_pacotes);
        System.out.println("Ocupação: " + ocupacao / tempo);
        System.out.println("Ocupacao WEB: " + ocupacao_web / tempo);
        System.out.println("Ocupacao CBR: " + ocupacao_cbr / tempo);
        System.out.println("Ocupacao TOTAL: " + (ocupacao_cbr + ocupacao_web) / tempo);
        //Dados Web
        ew_saida_web.soma_areas += ew_saida_web.qtd_pacotes * (tempo - ew_saida_web.tempo_anterior);
        ew_chegada_web.soma_areas += ew_chegada_web.qtd_pacotes * (tempo - ew_chegada_web.tempo_anterior);

        double en_final = en_web.soma_areas / tempo;
        double ew = ew_chegada_web.soma_areas - ew_saida_web.soma_areas;
        ew = ew / ew_chegada_web.qtd_pacotes;

        double lambda = ew_chegada_web.qtd_pacotes / tempo;
        System.out.println("\n===========little Web===============");
        System.out.printf("Quantidades de pacotes Web: %.0f\n", ew_chegada_web.qtd_pacotes);
        System.out.printf("E[N] Web = %.15f\n", en_final);
        System.out.printf("E[W] Web = %.10f\n", ew);
        System.out.println("Lambda Web = " + lambda);
        System.out.printf("\nValidação little λ: %.15f\n", (en_final - (lambda * ew)));
        double ll = ((ew_chegada_web.qtd_pacotes / tempo) * ((ew_chegada_web.soma_areas - ew_saida_web.soma_areas) / ew_chegada_web.qtd_pacotes));
        System.out.println("Little: " + ll);
        double eps = 0.03;
        if (Math.abs(ll - (en_web.soma_areas / tempo)) < eps) {
            System.out.println("Good simulation");
        } else {
            System.out.println("Bad simulation");
        }

        //Dados Cbr
        ew_saida_cbr.soma_areas += ew_saida_cbr.qtd_pacotes * (tempo - ew_saida_cbr.tempo_anterior);
        ew_chegada_cbr.soma_areas += ew_chegada_cbr.qtd_pacotes * (tempo - ew_chegada_cbr.tempo_anterior);

        en_final = en_cbr.soma_areas / tempo;
        ew = ew_chegada_cbr.soma_areas - ew_saida_cbr.soma_areas;
        ew = ew / ew_chegada_cbr.qtd_pacotes;

        lambda = ew_chegada_cbr.qtd_pacotes / tempo;
        System.out.println("\n===========little Cbr===============");
        System.out.printf("Quantidades de pacotes Cbr: %.0f\n", ew_chegada_cbr.qtd_pacotes);
        System.out.printf("E[N] Cbr = %.15f\n", en_final);
        System.out.printf("E[W] Cbr = %.10f\n", ew);
        System.out.println("Lambda Cbr = " + lambda);
        System.out.printf("\nValidação little λ: %.15f\n", (en_final - (lambda * ew)));
        ll = ((ew_chegada_cbr.qtd_pacotes / tempo) * ((ew_chegada_cbr.soma_areas - ew_saida_cbr.soma_areas) / ew_chegada_cbr.qtd_pacotes));
        System.out.println("Little: " + ll);
        eps = 0.03;
        if (Math.abs(ll - (en_cbr.soma_areas / tempo)) < eps) {
            System.out.println("Good simulation");
        } else {
            System.out.println("Bad simulation");
        }

        System.out.println("Quantidade de conexoes: " + qtd_total_conexoes);
        System.out.println("\n#####################Fim Simulação#######################\n");
    }
}
