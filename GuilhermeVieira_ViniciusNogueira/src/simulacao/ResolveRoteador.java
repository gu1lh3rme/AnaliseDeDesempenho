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

    Little en, ew_chegada, ew_saida;

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
        return tam*intervalo / link;
    }
    
    public double ocupacao_pct_Cbr(double tam, double link, double intervalo, double duracao) {
        return (tam*intervalo / link)*(duracao/intervalo);
    }

    double intervaloPacote(double link, double ocupacao, double tamMedioPacote) {
        double result = (link * ocupacao) / tamMedioPacote;
        return (1.0 / result);
    }
    
    public double gera_intervalo_cbr(double minimo, double maximo) {
        Random random = new Random();
        return random.nextDouble() * (maximo - minimo) + minimo;
    }

    public void Resolve() {
        Pacote inicio;
        ArrayList<Pacote> filaRoteador = new ArrayList();
        ArrayList<Conexao> filaConexoes = new ArrayList();
        //variavel para en
        this.en = new Little(0.0, 0.0, 0.0);
        //variável para ew chegada
        this.ew_chegada = new Little(0.0, 0.0, 0.0);
        //variável para ew saída
        this.ew_saida = new Little(0.0, 0.0, 0.0);

        //iniciando a semente para a geração
        //dos números pseudoaleatorios
        int semente = 1556915527;
        System.out.println("Semente: " + semente);
        gerador = new Random(semente);
        //tempo atual
        double tempo = 0.0;
        //tempo total
        double tempo_total = 10000.0;
        //System.out.println("Informe o tempo total de simulação: ");
        //scanf("%lF",  & tempo_total);
        
        //intervalo médio entre pacotes
        double intervalo = intervaloPacote(1250000, 0.6, 441);  //0.000441
        //System.out.println("Informe o intervalo médio de tempo (segundos) entre pacotes: ");
        //scanf("%lF",  & intervalo);
        //ajustando parametro para a exponencial
        intervalo = 1.0 / intervalo;    //2267,573696145
        //Contador de pacotes
        //  double cont_pcts = 0.0;

        //Tam pacote gerado
        double tam_pct;
        /*
        double cont_pct_550 = 0.0;
        double cont_pct_40 = 0.0;
        double cont_pct_1500 = 0.0;
         */

        //Tamanho do link do roteador 1250000 ocupacao de 80%
        double link = 10.0;
        //System.out.println("Tamanho do link (Mbps): ");
        //scanf("%lF",  & link);

        //tempo de chegada do proximo pacote
        //ao sistema
        double chegada_proximo_pct = chegada_pct(intervalo);
        //System.out.println("Chegada do primeiro pacote: %lF\n", chegada_proximo_pct);
        double chegada_proximo_pct_cbr = gera_intervalo_cbr(0.01, 0.02);
        //Define a taxa de chegada da próxima conexao, duração e qtd pcts da conexão
        double duracao_conexao = 12;
        double chegada_proxima_conexao = chegada_proximo_pct_cbr;  //Primeira conexão começa quando o primeiro cbr chega
        double intervalo_conexao_cbr = 4;
        double qtd_pcts_conexao = duracao_conexao / chegada_proximo_pct_cbr;
        double intervalo_cbr = chegada_proximo_pct_cbr;
        double saida_pct_atendimento = 0.0;
        double ocupacao = 0.0;
        int qtd_total_conexoes = 0;

        
        double ocweb = ocupacao_pct_Web(441, 1250000, intervalo);
        double ocCbr = ocupacao_pct_Cbr(1200, 1250000, chegada_proximo_pct_cbr, duracao_conexao);
        System.out.println("Ocupacao Web: " + ocweb);
        System.out.println("Ocupacao CBR: " + ocCbr);
        System.out.println("Ocupação total: "+ (ocweb+ocCbr));
        System.out.println("\n#####################Simulação#######################");

        while (tempo <= tempo_total) {
            //roteador vazio. Logo avanço no tempo de chegada do
            //proximo pacote
            if (filaRoteador.isEmpty()) {
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
                chegada_proxima_conexao += intervalo_conexao_cbr;
                tam_pct = (1200.0 * 8.0) / (1000000.0);
                filaConexoes.add(new Conexao(chegada_proximo_pct_cbr, qtd_pcts_conexao, duracao_conexao, tam_pct, tempo));
            }

            //chegada de pacote
            if (tempo == chegada_proximo_pct) {
                //roteador estava livre
                //  System.out.println("Chegada de pacote no tempo: %lF\n", tempo);
                tam_pct = gera_tam_pct();

                if (filaRoteador.isEmpty()) {

                    //descobrir o tamanho do pacote
                    //gerando o tempo em que o pacote atual sairá do sistema
                    saida_pct_atendimento = tempo + tam_pct / link;

                    ocupacao += saida_pct_atendimento - tempo;
                }
                //pacote colocado na fila
                inicio = new Pacote(tam_pct, tempo);
                filaRoteador.add(inicio);
                // inserir(inicio, fim, tam_pct);
                //gerar o tempo de chegada do próximo
                chegada_proximo_pct = tempo + chegada_pct(intervalo);

                //cálculo little -- E[N]
                en.soma_areas += en.qtd_pacotes * (tempo - en.tempo_anterior);
                en.qtd_pacotes++;
                en.tempo_anterior = tempo;

                ew_chegada.soma_areas += ew_chegada.qtd_pacotes * (tempo - ew_chegada.tempo_anterior);
                ew_chegada.qtd_pacotes++;
                ew_chegada.tempo_anterior = tempo;
                
                //atualizar próxima conexão
            } else if (tempo == chegada_proximo_pct_cbr) {
                //chega pct cbr 1200 bytes
                tam_pct = (1200.0 * 8.0) / (1000000.0);

                if (filaRoteador.isEmpty()) {
                    //descobrir o tamanho do pacote
                    //gerando o tempo em que o pacote atual sairá do sistema
                    saida_pct_atendimento = tempo + tam_pct / link;

                    ocupacao += saida_pct_atendimento - tempo;
                }

                //pacote colocado na fila
                inicio = proxPacote(filaConexoes);
                if (inicio == null) {

                }
                filaRoteador.add(inicio);
                // inserir(inicio, fim, tam_pct);
                //gerar o tempo de chegada do próximo
                chegada_proximo_pct_cbr += intervalo_cbr;

                //cálculo little  --- E[N]
                en.soma_areas += en.qtd_pacotes * (tempo - en.tempo_anterior);
                en.qtd_pacotes++;
                en.tempo_anterior = tempo;

                //cálculo little  --- E[W] chegada
                ew_chegada.soma_areas += ew_chegada.qtd_pacotes * (tempo - ew_chegada.tempo_anterior);
                ew_chegada.qtd_pacotes++;
                ew_chegada.tempo_anterior = tempo;

            } else { //saida de pacote
                //    System.out.println("Saída de pacote no tempo: %lF\n", tempo);
                //remover(inicio);
                filaRoteador.remove(0);

                if (!filaRoteador.isEmpty()) {
                    //Obtem o tamanho do pacote
                    tam_pct = filaRoteador.get(0).tamanho;
                    //gerando o tempo em que o pacote atual sairá do sistema
                    saida_pct_atendimento = tempo + tam_pct / link;

                    ocupacao += saida_pct_atendimento - tempo;
                }
                //cálculo little -- E[N]
                en.soma_areas += en.qtd_pacotes * (tempo - en.tempo_anterior);
                en.qtd_pacotes--;
                en.tempo_anterior = tempo;

                //cálculo little  --- E[W] saída
                ew_saida.soma_areas += ew_saida.qtd_pacotes * (tempo - ew_saida.tempo_anterior);
                ew_saida.qtd_pacotes++;
                ew_saida.tempo_anterior = tempo;

            }
        }

        ew_saida.soma_areas += ew_saida.qtd_pacotes * (tempo - ew_saida.tempo_anterior);
        ew_chegada.soma_areas += ew_chegada.qtd_pacotes * (tempo - ew_chegada.tempo_anterior);

        double en_final = en.soma_areas / tempo;
        double ew = ew_chegada.soma_areas - ew_saida.soma_areas;
        ew = ew / ew_chegada.qtd_pacotes;

        double lambda = ew_chegada.qtd_pacotes / tempo;

        System.out.println("Ocupacao: " + ocupacao / tempo);
        System.out.println("\n===========little===============");
        System.out.printf("E[N] = %.15f\n",  en_final);
        System.out.printf("E[W] = %.10f\n" , ew);
        System.out.println("Lambda = " + lambda);
        System.out.println("\n=======================");
        System.out.printf("Validação little λ: %.15f\n" , (en_final - (lambda * ew)));
        System.out.println("Quantidade de conexoes: " + qtd_total_conexoes);
        double ll = ((ew_chegada.qtd_pacotes / tempo) * ((ew_chegada.soma_areas - ew_saida.soma_areas) / ew_chegada.qtd_pacotes));
        System.out.println("Little: little = "+ ll);
        double eps = 0.03;
        if (Math.abs(ll-(en.soma_areas / tempo)) < eps){
            System.out.println ("Good simulation");
        }else{
            System.out.println("Bad simulation - ");
        }
    }
}