# Regras e Mecânicas do Colony System

Este documento detalha o funcionamento, as regras e a arquitetura do ecossistema multiagente da simulação da colônia, baseado no código fonte atual (`Testes/src/com/colony/`).

## 1. Visão Geral e Interface

A colônia é gerenciada por uma arquitetura multiagente (utilizando o framework JADE) dividida em três papéis principais:
*   **WorkerAgent (Trabalhador / NPC):** A unidade física da colônia. Executa as ordens, movimenta-se pelo mapa, gasta energia e ganha experiência.
*   **ManagerAgent (Gerente):** O cérebro logístico. Recebe as necessidades da colônia, cria uma fila de tarefas (Tasks), calcula urgências e prazos, e distribui as missões para o melhor trabalhador disponível.
*   **AnalystAgent (Analista):** O auditor e planejador urbano. Analisa o terreno, decide onde construir, verifica se os trabalhadores precisam de casas/oficinas, e audita rigorosamente o trabalho concluído.

### Interface do Usuário (GUI)
*   As tarefas (Tasks) no painel de controle são organizadas em três sub-abas (Tabs) para separar as missões:
    *   **Em Espera:** Tarefas pendentes ou na fila aguardando ação.
    *   **Sendo Feitas:** Tarefas em execução ativa por um colono.
    *   **Concluídas:** Tarefas auditadas e terminadas com sucesso.
*   **Rodapé (Footer):** Exibe informações em tempo real calculadas a partir das tarefas em andamento, incluindo contagem total consolidada.

---

## 2. Regras de Movimentação, Infraestrutura e Construção

### Loteamento de Construções (Espaçamento)
*   **Regra do 1-Tile:** Nenhuma construção pode encostar em outra. É obrigatório haver pelo menos 1 tile livre (espaço de chão) entre o perímetro de qualquer edifício e o de seus vizinhos. Isso garante vielas e passagens livres pela vila.

### Edifícios, Paredes e Portas
*   Todas as construções fechadas (como Casas, Hospitais, Oficinas) possuem **Paredes** e **Portas**.
*   **Paredes:** Ocupam a borda externa da construção e são intransponíveis (`blocksMovement = true`).
*   **Portas:** Localizam-se sempre no centro da face inferior (sul) do edifício. É o único tile aberto por onde o NPC pode entrar ou sair do interior livre da construção.

### Estradas (Roads)
*   Trabalhadores podem construir **Estradas**.
*   **Benefícios:** Mover-se por uma estrada aumenta a velocidade de locomoção e reduz significativamente o consumo de energia da caminhada.
*   **Regras de Construção:** Devem formar **linhas contínuas**, com no máximo pontos de bifurcação, evitando poluição caótica no mapa.

### Navegação (Pathfinding A*)
*   Os colonos utilizam um sistema de rotas (A*) que reconhece e desvia de paredes e obstáculos, garantindo que não atravessem construções indevidamente.

---

## 3. Regras dos Trabalhadores (Workers)

### Atributos de Sobrevivência e Descanso
*   **Energia (Energy):** Varia de 0 a 100.
    *   Cada ação de trabalho gasta energia.
    *   Se a energia cair para `<= 30`, o trabalhador entra em modo de preservação e procura descansar.
    *   **Descanso Obrigatório em Casa:** Para ter um descanso eficiente, o NPC **deve caminhar fisicamente para dentro de sua casa**. Se não conseguir chegar ou não tiver casa, ele dorme no relento recuperando muito menos energia e gastando mais tempo.
*   **Saúde (Health):** Varia de 0 a 100. Morte iminente (<= 10) força a busca por tratamento médico.

### Logística de Oficinas e Materiais
*   **Trabalho Interno:** Um colono com profissão específica (ex: Ferreiro, Carpinteiro) **obrigatoriamente** deve se deslocar para dentro do perímetro de sua **Oficina** para executar a ação de craft.
*   **Busca de Materiais:** O material não aparece magicamente na mão do colono. Antes de iniciar um trabalho na oficina, o trabalhador precisará andar até um **Armazém (Stockpile)** para buscar os recursos necessários. Se não houver armazém, a tarefa falha ou atrasa.

### Profissões e Sistema de Experiência (Skills)
*   Trabalhar gera XP. Subir de Nível (Level) faz o colono trabalhar mais rápido e com mais qualidade, ganhando prioridade com o Gerente.
*   Se for designado para uma tarefa que não sabe, ele a aprenderá no nível 1.

---

## 4. Regras de Distribuição de Trabalho (Manager)

O Gerente realiza um "leilão" logístico baseado em **Score**:
*   Apenas trabalhadores desocupados e com Energia `>= 30` são considerados.
*   **Habilidade:** Dá +50 pontos iniciais, mais +10 por nível.
*   **Distância e Disposição:** Favorece quem estiver mais perto e com mais energia.
*   **Urgência:** Multiplica o peso da habilidade, exigindo "funcionários seniores" para problemas críticos.

---

## 5. Auditoria, Prazos e Expansão de Zonas (Analista)

O AnalystAgent impõe regras estritas de qualidade:
*   **Auditoria de Tarefas:** Se a construção de um prédio for reportada como pronta, mas o progresso real for `< 100%`, o Analista **Reprova** a tarefa. Tarefas com prazos estourados ou executadas com a skill errada também são rejeitadas.
*   **Zonas de Construção:** Os colonos podem expandir as zonas ativamente. Novas áreas podem ser demarcadas para planejamento.
*   **Gestão de Casas e Oficinas:** O Analista conta os trabalhadores "Sem-Teto" e emite ordens para construção de casas (até 2 por vez). Se uma profissão necessita de uma oficina específica que não existe, o Analista ordena a construção automaticamente.
*   **Zonas de Matéria-Prima:** Mapeamento autônomo de jazidas e florestas para trabalhadores ociosos.
