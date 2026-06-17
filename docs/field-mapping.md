# Mapeamento confirmado de campos do Brasfoot

Este arquivo registra mapeamentos confirmados por cruzamento entre:

1. save real: `~/Documentos/Brasfoot22-23/sav/not.s22`
2. objetos desserializados pelo `SaveFileService`/Kryo
3. bytecode do jar do Brasfoot (`lib/brasfoot.jar` / `brasfoot_files/best/F.class`)
4. valores observados em lote via reflexão

## Player (`best.F`)

### Salário atual

Campo confirmado: `et`

Constante adicionada no projeto:

```java
public static final String PLAYER_SALARY = "et";
```

Evidências:

- Em `best.F`, o getter obfuscado `fj()` retorna diretamente o campo `et`:

```text
public int fj();
  0: aload_0
  1: getfield #364 // Field et:I
  4: ireturn
```

- O setter obfuscado `ae(int)` grava diretamente em `et`:

```text
public void ae(int);
  0: aload_0
  1: iload_1
  2: putfield #364 // Field et:I
  5: return
```

- O método `fJ()` recalcula esse mesmo campo usando overall (`eq`), idade (`em`), posição (`en`), estrelas (`ek`/`el`) e a configuração do jogo `best/f.isSalarioMensal()`:

```text
349: getstatic     #259 // Field c/Aa.SR:Lbest/f;
352: invokevirtual #502 // Method best/f.isSalarioMensal:()Z
355: ifeq          368
358: aload_0
359: iload_1
360: iconst_4
361: imul
362: putfield      #364 // Field et:I
...
368: aload_0
369: iload_1
370: putfield      #364 // Field et:I
```

Isso amarra semanticamente `et` a salário: o jogo verifica explicitamente `isSalarioMensal()` antes de gravar o campo.

- Amostra real do save `not.s22`, comparando campo direto `et` com getter `fj()`:

```text
12 de Octubre | Jerónimo Alba         | et=84000 | fj()=84000
12 de Octubre | Felipe Antônio        | et=68400 | fj()=68400
12 de Octubre | Luis Carlos Samuel    | et=65720 | fj()=65720
12 de Octubre | Ángel Villalba        | et=58800 | fj()=58800
12 de Octubre | Bernardino Cáceres    | et=60760 | fj()=60760
```

Conclusão: para jogador `best.F`, salário atual é o campo `et`.

### Posição

Campo confirmado: `en`

Constante já usada no projeto:

```java
public static final String PLAYER_POSITION = "en";
```

Tabela confirmada no save `not.s22`, usando jogadores do time `12 de Octubre` conferidos manualmente na UI do jogo:

```text
Julio César          | goleiro  | en=0 | getPosicao()=0
Saulo Coelho         | lateral  | en=1 | getPosicao()=1
Luis Carlos Samuel   | zagueiro | en=2 | getPosicao()=2
Jerónimo Alba        | meia     | en=3 | getPosicao()=3
Felipe Antônio       | atacante | en=4 | getPosicao()=4
```

Conclusão: para jogador `best.F`, posição é o campo `en` com enum:

```text
0 = Goleiro
1 = Lateral
2 = Zagueiro
3 = Meia
4 = Atacante
```

Evidência adicional: o bytecode da classe `best.F` tem getter público `getPosicao()` que retorna esse campo e setter `setPosicao(int)` que grava nele.

### Energia / condição

Campo confirmado: `eK`

Constante usada no projeto:

```java
public static final String PLAYER_ENERGY = "eK";
```

Evidências:

- Em `best.F`, o campo inicia em `100`, o getter obfuscado `fp()` retorna `eK`, e o setter obfuscado `ai(int)` grava diretamente em `eK`.
- Os métodos `aj(int)` e `ak(int)` reduzem/aumentam `eK` com limite superior `100`, comportamento compatível com energia/condição.
- A tabela original de jogadores (`b.Q`) tem coluna `Energia` e, para essa coluna, retorna `((F) jogador).fp()`.
- Em amostra real do save `not.s22`, `Ibrahim Dresevic` tem `eK=100`, enquanto `ep=-1`; portanto `ep` não é a energia exibida pelo Brasfoot original.

Conclusão: para jogador `best.F`, energia/condição editável deve usar `eK`, não `ep`.

### Habilidades individuais

Campos confirmados: `eA..eG`

Esses campos são usados quando a opção do jogo `Aa.SR.isHabilidadeIndividual()` está ativa. A escala observada no bytecode é `0..100`: os métodos de evolução `aG(int)` limitam cada campo a `100`, e a geração inicial `j(int,int)` também limita os valores acima de `100`.

| Campo | Getter | Setter | Rótulo original | Constante sugerida |
| --- | --- | --- | --- | --- |
| `eA` | `gK()` | `aJ(int)` | Goleiro / Gol | `PLAYER_SKILL_GOALKEEPING` |
| `eB` | `gJ()` | `aI(int)` | Velocidade / Vel | `PLAYER_SKILL_SPEED` |
| `eC` | `gL()` | `aK(int)` | Técnica / Tec | `PLAYER_SKILL_TECHNIQUE` |
| `eD` | `gM()` | `aL(int)` | Passe / Pas | `PLAYER_SKILL_PASSING` |
| `eE` | `gN()` | `aM(int)` | Desarme / Des | `PLAYER_SKILL_TACKLING` |
| `eF` | `gO()` | `aN(int)` | Armação / Arm | `PLAYER_SKILL_PLAYMAKING` |
| `eG` | `gP()` | `aO(int)` | Finalização / Fin | `PLAYER_SKILL_FINISHING` |

Evidências:

- `a.eg` monta a tela detalhada com rótulos completos e getters: `Goleiro -> gK()`, `Desarme -> gN()`, `Passe -> gM()`, `Armação -> gO()`, `Finalização -> gP()`, `Velocidade -> gJ()`, `Técnica -> gL()`.
- `a.jm` e `components.M` repetem os rótulos abreviados `Gol/Des/Arm/Fin/Vel/Tec/Pas` com os mesmos getters.
- `best.F.aF(int)` usa a ordem interna de treino/evolução: `0=gK/eA`, `1=gJ/eB`, `2=gL/eC`, `3=gM/eD`, `4=gN/eE`, `5=gO/eF`, `6=gP/eG`.
- `best.F.j(int,int)` inicializa `eA..eG` com fórmulas por posição e características inatas, reforçando que são atributos/habilidades do jogador.

Conclusão: `eA..eG` são as habilidades individuais numéricas do jogador.

### Campos relacionados que NÃO são salário

- `eu`: getter `fk()`, recalculado por `fK()`. Pelo tamanho dos valores e fórmula, parece valor de mercado/passe.
- `ev`: getter `fl()`, recebe `eu` em `fm()`. O uso ainda não foi confirmado; descartar por enquanto para edição de salário/posição.

Exemplo real:

```text
Jerónimo Alba | et=84000 fj()=84000 | eu=7977600 fk()=7977600 | ev=701607 fl()=701607
```

`et` fica na escala de salário; `eu` fica na escala de valor de mercado. `ev` permanece sem utilidade confirmada.

## Team (`best.ah`) / Manager (`best.al`)

### Vínculo técnico-time e time humano

Campos confirmados:

| Classe | Campo | Uso confirmado | Constante |
| --- | --- | --- | --- |
| `best.ah` | `mU` | ID interno do time retornado por `lk()` | `TEAM_ID` |
| `best.ah` | `mW` | flag de time controlado por humano | `TEAM_IS_HUMAN` |
| `best.ah` | `na` | ID do técnico associado ao time | `TEAM_MANAGER_ID` |
| `best.ah` | `mZ` | referência transient para o técnico associado | `TEAM_MANAGER_REFERENCE` |
| `best.al` | `nU` | ID interno do técnico retornado por `lT()` | `MANAGER_ID` |
| `best.al` | `nV` | referência transient para o time atual | `MANAGER_CURRENT_TEAM` |
| `best.al` | `bW` | cache serializado do ID do time atual | `MANAGER_CURRENT_TEAM_ID` |
| `best.f` | `ak` | lista de times controlados por humano retornada por `aN()` | `HUMAN_TEAMS_LIST` |

Evidências:

- Em `best.ah`, `lk()` retorna diretamente `mU`, e `bX(int)` grava em `mU`.
- Em `best.f.x(int)`, o jogo procura times comparando o parâmetro com `best.ah.lk()`, confirmando que referências para time usam `mU`, não `na`.
- Em `best.ah`, `jZ()` retorna `mW`, e `k(Boolean)` grava em `mW`.
- Em `best.ah.ka()`, quando `mZ` está nulo, o jogo resolve o técnico por `best.f.y(na)`, então `na` é o ID do técnico associado ao time.
- Em `best.ah.h(best.al)`, o jogo grava `mZ` e sincroniza `na` com `best.al.lT()`; quando o técnico é nulo, grava `na=-1`.
- Em `best.al`, `lT()` retorna `nU`.
- Em `best.al.fg()`, quando `nV` está nulo e `bW >= 0`, o jogo resolve o time por `best.f.x(bW)`; portanto `bW` é o ID serializado do time atual.
- Em `best.al.n(best.ah)`, o jogo grava `nV` e sincroniza `bW` com `best.ah.lk()`; quando o time é nulo, grava `bW=-1`.
- Em `best.f.aN()`, o jogo retorna a lista `ak`, e métodos de contratação/demissão adicionam/removem times dessa lista ao alternar controle humano.

Conclusão: transferir técnico humano de time exige atualizar o técnico (`mW`, `nV`, `bW`), o time antigo/novo (`mW`, `na`, `mZ`) e a lista raiz `ak`. Alterar apenas `best.al.nU` corrompe o vínculo porque `nU` identifica o técnico, não o time atual.

### Troféus / histórico do técnico

Campos confirmados:

| Classe | Campo | Uso confirmado | Constante |
| --- | --- | --- | --- |
| `best.al` | `cA` | lista de itens `best.ao` retornada por `cT()` | `MANAGER_TROPHIES` |
| `best.ao` | `ae` | temporada/ano do item | `MANAGER_TROPHY_YEAR` |
| `best.ao` | `w` | tipo de competição, copiado de `best.at.b()` | `MANAGER_TROPHY_COMPETITION_TYPE` |
| `best.ao` | `dz` | país/divisão/variante conforme o tipo | `MANAGER_TROPHY_VARIANT` |
| `best.ao` | `bW` | ID do time associado ao título | `MANAGER_TROPHY_TEAM_ID` |
| `best.ao` | `Y` | referência opcional para a competição `best.at` | `MANAGER_TROPHY_COMPETITION_REFERENCE` |

Evidências:

- Em `best.al.q(best.at)`, o jogo cria `new best.ao()`, grava `ae` com o ano atual (`best.f.H()`), `bW` com o time, `w` com `best.at.b()`, `Y` com a competição e `dz` com país/divisão/variante conforme a competição; depois adiciona o item em `cA`.
- Em `best.al.cT()`, o getter retorna diretamente `cA`.
- No save real `REAL BRASFOOT 2026/sav/eae.s22`, `root.al[0]` é o técnico `ruan` e tem `cA` com 42 itens `best.ao`, compatível com os títulos visíveis no jogo.

Conclusão: mudar/remover troféus existentes do técnico deve editar/remover itens de `best.al.cA`. Ao editar um item existente, preservar `Y` evita perder a referência de competição. Itens novos podem ser criados com os campos numéricos; se `Y` ficar nulo, o save mantém os dados essenciais, mas o nome textual da competição pode depender do jogo reconstruir ou tolerar essa referência ausente.
