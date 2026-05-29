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
