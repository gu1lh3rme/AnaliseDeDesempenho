/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.c
 * Author: 2014.1.08.012
 *
 * Created on 26 de Abril de 2019, 16:29
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include "fila.h"

typedef struct little_ {
    double tempo_anterior;
    double soma_areas;
    double qtd_pacotes;
} little;



//Numero aleatorio entre 0 e 1

double aleatorio() {
    double u;
    u = rand() % RAND_MAX;
    u = u / RAND_MAX;
    u = 1.0 - u;

    return (u);
}

/**
 * 
 * @param parametro_l da exponencial
 * @return intervalo de tempo, com media tendendo ao intervalo
 * informado pelo usuário
 */

double chegada_pct(double l) {
    return ((-1.0 / l))*log(aleatorio());
}

/**
 * 
 * @param parametro_l da exponencial
 * @return intervalo de tempo, com media tendendo ao intervalo
 * informado pelo usuário
 */

double gera_tam_pct() {
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
 * 
 * @param a
 * @param b
 * @return  menor dentre os valores
 */
double minimo(double a, double b) {
    if (a <= b)
        return a;
    return b;
}

void inicia_little(little *l){
    l->qtd_pacotes = 0.0;
    l->soma_areas = 0.0;
    l->tempo_anterior = 0.0;
}

int main(int argc, char** argv) {
    pacote **inicio = malloc(sizeof (pacote *));
    pacote **fim = malloc(sizeof (pacote *));
    *inicio = NULL;
    *fim = NULL;
    
    //variavel para en
    little en;
    //variável para ew chegada
    little ew_chegada;
    //variável para ew saída
    little ew_saida;
    inicia_little(&en);
    inicia_little(&ew_chegada);
    inicia_little(&ew_saida);

    //iniciando a semente para a geração
    //dos números pseudoaleatorios
    int semente = 1556915527;
    printf("Semente: %d\n", semente);
    srand(semente);
    //tempo atual
    double tempo = 0.0;
    //tempo total
    double tempo_total;
    printf("Informe o tempo total de simulação: ");
    scanf("%lF", &tempo_total);

    //intervalo médio entre pacotes
    double intervalo;
    printf("Informe o intervalo médio de tempo (segundos) entre pacotes: ");
    scanf("%lF", &intervalo);
    //ajustando parametro para a exponencial
    intervalo = 1.0 / intervalo;
    //Contador de pacotes
    //  double cont_pcts = 0.0;

    //Tam pacote gerado
    double tam_pct;
    /*
        double cont_pct_550 = 0.0;
        double cont_pct_40 = 0.0;
        double cont_pct_1500 = 0.0;
     */

    //Tamanho do link do roteador
    double link;
    printf("Tamanho do link (Mbps): ");
    scanf("%lF", &link);

    //Fila, onde fila == 0 indica roteador vazio, fila == 1
    //indica 1 pacote, já em transmissão;
    //fila > 1 indica 1 pacote em transmissão e demais em espera
    // double fila = 0.0;

    //tempo de chegada do proximo pacote
    //ao sistema
    double chegada_proximo_pct = chegada_pct(intervalo);
    //printf("Chegada do primeiro pacote: %lF\n", chegada_proximo_pct);
    double chegada_proximo_pct_cbr = 0.02;

    double saida_pct_atendimento = 0.0;
    double ocupacao = 0.0;

    while (tempo <= tempo_total) {
        //roteador vazio. Logo avanço no tempo de chegada do
        //proximo pacote
        if (*inicio == NULL) {
            tempo = minimo(chegada_proximo_pct, chegada_proximo_pct_cbr);
        } else {
            //Há fila!
            tempo = minimo(minimo(chegada_proximo_pct_cbr, chegada_proximo_pct), saida_pct_atendimento);
        }

        //chegada de pacote
        if (tempo == chegada_proximo_pct) {
            //roteador estava livre
            //  printf("Chegada de pacote no tempo: %lF\n", tempo);
            tam_pct = gera_tam_pct();
            if (*inicio == NULL) {
                //descobrir o tamanho do pacote
                //gerando o tempo em que o pacote atual sairá do sistema
                saida_pct_atendimento = tempo + tam_pct / link;

                ocupacao += saida_pct_atendimento - tempo;
            }
            //pacote colocado na fila
            inserir(inicio, fim, tam_pct);
            //gerar o tempo de chegada do próximo
            chegada_proximo_pct = tempo + chegada_pct(intervalo);

            //cálculo little -- E[N]
            en.soma_areas += en.qtd_pacotes * (tempo - en.tempo_anterior);
            en.qtd_pacotes++;
            en.tempo_anterior = tempo;
            
            ew_chegada.soma_areas += ew_chegada.qtd_pacotes * (tempo - ew_chegada.tempo_anterior);
            ew_chegada.qtd_pacotes++;
            ew_chegada.tempo_anterior = tempo;

        } else if (tempo == chegada_proximo_pct_cbr) {
            //chega pct cbr 1200 bytes
            tam_pct = (1200.0 * 8.0) / (1000000.0);
            if (*inicio == NULL) {
                //descobrir o tamanho do pacote
                //gerando o tempo em que o pacote atual sairá do sistema
                saida_pct_atendimento = tempo + tam_pct / link;

                ocupacao += saida_pct_atendimento - tempo;
            }
            //pacote colocado na fila
            inserir(inicio, fim, tam_pct);
            //gerar o tempo de chegada do próximo
            chegada_proximo_pct_cbr += 0.02;

            //cálculo little  --- E[N]
            en.soma_areas += en.qtd_pacotes * (tempo - en.tempo_anterior);
            en.qtd_pacotes++;
            en.tempo_anterior = tempo;
            
            //cálculo little  --- E[W] chegada
            ew_chegada.soma_areas += ew_chegada.qtd_pacotes * (tempo - ew_chegada.tempo_anterior);
            ew_chegada.qtd_pacotes++;
            ew_chegada.tempo_anterior = tempo;
        } else { //saida de pacote
            //    printf("Saída de pacote no tempo: %lF\n", tempo);
            remover(inicio);

            if (*inicio != NULL) {
                //Obtem o tamanho do pacotehttps://github.com/gu1lh3rme/AnaliseDeDesempenho.git
                tam_pct = (*inicio)->tamanho;
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
        //  printf("===========================================\n\n");
        //   getchar();
    }
    
    ew_saida.soma_areas += ew_saida.qtd_pacotes * (tempo - ew_saida.tempo_anterior);
    ew_chegada.soma_areas += ew_chegada.qtd_pacotes * (tempo - ew_chegada.tempo_anterior);
    
    double en_final = en.soma_areas/tempo;
    double ew = ew_chegada.soma_areas - ew_saida.soma_areas;
    ew = ew/ew_chegada.qtd_pacotes;
    
    double lambda = ew_chegada.qtd_pacotes / tempo;
    
    printf("Ocupacao: %lF\n", ocupacao / tempo);
    printf("\n===========little===============\n");
    printf("E[N] = %lF\n", en_final);
    printf("E[W] = %lF\n", ew);
    printf("Lambda = %lF\n", lambda);
    printf("\n=======================\n");
    printf("Validação little: %.10lF\n", en_final - (lambda * ew));
    /*
        printf("Pacotes gerados: %lF\n", cont_pcts);
        printf("Media do intervalo: %lF\n", tempo / cont_pcts);
        printf("Proporção de pacotes com tamanho 550: %lF\n", cont_pct_550 / cont_pcts);
        printf("Proporção de pacotes com tamanho 40: %lF\n", cont_pct_40 / cont_pcts);
        printf("Proporção de pacotes com tamanho 1500: %lF\n", cont_pct_1500 / cont_pcts);
     */

    /*
     Saída:
    Semente: 1556915527
    Informe o tempo total de simulação: 1000                    
    Informe o intervalo médio de tempo (segundos) entre pacotes: 0.000441
    Tamanho do link (Mbps): 10
    Ocupacao: 0.847439

===========little===============
E[N] = 5.148129

     */
    return (EXIT_SUCCESS);
}

