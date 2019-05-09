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
    if (a < b)
        return a;
    return b;
}

int main(int argc, char** argv) {
    pacote **inicio = malloc(sizeof(pacote *));
    pacote **fim = malloc(sizeof(pacote *));
    
    insere(inicio, fim, 500);
    insere(inicio, fim, 600);
    insere(inicio, fim, 700);
    insere(inicio, fim, 800);
    insere(inicio, fim, 900);
    
    //iniciando a semente para a geração
    //dos números pseudoaleatorios
    int semente = time(time(NULL));
    srand(time(NULL));
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
    double fila = 0.0;

    //tempo de chegada do proximo pacote ao sistema
    double chegada_proximo_pct = chegada_pct(intervalo);
    double saida_pct_atendimento = 0.0;
    double ocupacao = 0.0;

    while (tempo <= tempo_total) {
        //roteador vazio. Logo avanço no tempo de chegada do
        //proximo pacote
        if (fila == 0.0) {
            tempo = chegada_proximo_pct;
        } else {
            //Há fila!
            tempo = minimo(chegada_proximo_pct, saida_pct_atendimento);
        }

        //chegada de pacote
        if (tempo == chegada_proximo_pct) {
            //roteador estava livre
          //  printf("Chegada de pacote no tempo: %lF\n", tempo);
            if (fila == 0.0) {
                //descobrir o tamanho do pacote
                tam_pct = gera_tam_pct();
                //gerando o tempo em que o pacote atual sairá do sistema
                saida_pct_atendimento = tempo + tam_pct / link;
                
                ocupacao += saida_pct_atendimento - tempo;
            }
            //pacote colocado na fila
            fila++;
           // printf("Fila: %lF\n", fila);
            //gerar o tempo de chegada do próximo
            chegada_proximo_pct = tempo + chegada_pct(intervalo);
            
        } else { //saida de pacote
        //    printf("Saída de pacote no tempo: %lF\n", tempo);
            fila--;
       //     printf("Fila: %lF\n",fila);
            if (fila > 0.0) {
                // descobrir o tamanho do pacote
                tam_pct = gera_tam_pct();
                //gerando o tempo em que o pacote atual sairá do sistema
                saida_pct_atendimento = tempo + tam_pct / link;
                
                ocupacao += saida_pct_atendimento - tempo;
            }
        }
      //  printf("===========================================\n\n");
     //   getchar();
    }
    
    printf("Ocupacao: %lF\n", ocupacao/tempo);
/*
    printf("Pacotes gerados: %lF\n", cont_pcts);
    printf("Media do intervalo: %lF\n", tempo / cont_pcts);
    printf("Proporção de pacotes com tamanho 550: %lF\n", cont_pct_550 / cont_pcts);
    printf("Proporção de pacotes com tamanho 40: %lF\n", cont_pct_40 / cont_pcts);
    printf("Proporção de pacotes com tamanho 1500: %lF\n", cont_pct_1500 / cont_pcts);
*/
    return (EXIT_SUCCESS);
}

