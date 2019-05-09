#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include "fila.h"


pacote *aloca_pct(){
    return malloc(sizeof(pacote));
}

/**
 * 
 * @param inicio aponta o primeiro da fila
 * @param fim aponta o ultimo da fila
 * @param tamanho tamanho do pacote a ser inserido
 * @return 1 caso insira, 0 caso erro
 */
int insere(pacote **inicio, pacote **fim, double tamanho){
   //fila vazia
    if (*inicio == NULL) {
        *inicio = aloca_pct();
        if (*inicio == NULL) {
            return 0;
        }
        *fim = *inicio;
        (*inicio)->tamanho = tamanho;
        (*inicio)->prox = NULL;
        return 1;
    }else{
        pacote *tmp = aloca_pct();
        if (tmp == NULL) {
            return 0;
        }
        tmp->tamanho = tamanho;
        tmp->prox = NULL;
        
        (*fim)->prox = tmp;
        (*fim) = tmp;
        return 1;
    }

    return 0; 
}