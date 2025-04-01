# Fault Tolerance
- Provide correct service **even** in the presence of faults.
	- -> Redundancy, replication, checkpointing, etc.

## Replication
### Teórica
- Two or more copies of items that may be corrupted by a fault
- A mechanism that compares them and declares an error if they
differ
- Exemplos:
	- Duplicated circuitry
	-  Transmit messages twice
	-  Store data in two separate places (e.g. mirrored disks)

### O que fazer no Cruise Control?
- Variáveis duplicadas, triplicadas...? Para simular discos?
- FAZER

### Vantagens
...
### Desvantagens
...
## Redundancy
- Redundancy is the ingredient of Replication
### Teórica
#### Hardware redundancy
- TMR vs DMR vs Active hardware redundancy Standby sparing technique
	- -> Acho que a 3ª é a menos útil das 3 para o Cruise Control
- Talvez aplicat ambas TMR e DMR, caso o número de sensores ou resultados válidos seja maior que 2 usa-se TMR, se for igual a 2, recorre-se a DMR. Ou talvez usar antes o Pair-and-a-spare approach?
- USAR PAIR AND SPAIR WITH HOT STANDBY??? -> Aguardar resposta do professor
#### Information redundancy
- Existem várias técnicas mas foi recomendado pelo professor usar CRC, visto que é semelhante a hash e pode conseguir conseguir o problema devido a propriedades redundantes do CRC.
- TENTAR FAZER O CRC
#### Time redundancy
- Good for transient faults; no protection against permanent faults
- Re-execução do código parece ser o mais apropriado das opções.
- A re-execução do código será o código por inteiro ou repetir cada função depois dessa função acabar, haverá diferença?
- FAZER REEXECUÇÃO DE CADA FUNÇÃO VISTO QUE ASSIM O ERRO (DIFERENÇA DA EXECUÇÃO) É DETETADO MAIS CEDO
- Não percebi a parte do encode.
#### Software redundancy
- Consistency check vs capability check vs N-version programming.
	- Data consistency check parece ser fácil de implementar, resta saber quando se deve verificar, se é no início, se durante se no fim... 
	- Não sei se faz sentido implementar compatibility check.
	- N-version program
- FAZER CONSISTENCY CHECK E N-VERSION PROGRAMMING



## Checkpointing

- If a checkpoint failed, re-execute since the last checkpoint.
- Parece ser o melhor do tipo backward error recovery.
- RECOVERY BLOCK PARECE SER COMPLICADO DEMAIS TRAZENDO POUCAS VANTAGENS PARA A SITUAÇÃO DO REQUISITO.
## Input validation
### Descartar inputs errados
- Fazer com que inputs negativos sejam descartados.
	- 
- Fazer com que velocidades grandes sejam rejeitadas? 
- Adicionar Limite de velocidade inferior e  superior?

## validação output


### Importante
- Dizer talvez no relatório que estas soluções individualmente não são muito tolerantes a falhas, mas sim como um conjunto...
- Como lidamos com o single point of failure das soluções que usam comparadores?
	- Usar comparadores para os comparadores -> outro single point of failure, não pode ser.
- Soluções que tenham comparadores, para melhor assegurar que um valor está correto, caso hajam diferentes inputs no comparador estes devem estar desempatados, caso contrário não se saberá qual o correto, só se saberá que um deles está incorreto. Ver slides T2b, páginas 45,46.
- Preferível evitar os erros do que os corrigir
- Backward error recovery parece uma combinação interessante de checkpointing e n version...

DIIGO FEZ REDUNDANCIA TEMPORAL, VALIDAÇÃO DE INPUTS E OUPTUS


CONSIDERAR FAZER CHEKPOINTING TAMBEM PARA O CODIGO QUE VAI FAZER N VERSION CONTROL.