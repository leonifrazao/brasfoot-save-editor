# TODO

## Campos mapeados ainda nao expostos para edicao

### Jogadores (`best.F`)

- `eu`: provavel valor de mercado/passe, documentado mas ainda nao confirmado para edicao.
- `ev`: uso ainda nao confirmado, documentado para investigacao.
- `ep`: nao e energia da tabela original; observado como `-1` em jogador real do save `not.s22`. Manter fora da UI ate confirmar uso.
- `eR`: slot/faixa do jogador em campo, observado no debugger.
- `eN`: nota/avaliacao pos-jogo, observado no debugger.
- `status`: status do jogador, observado no debugger.

Observacao: `morale` existia na antiga entrada web, mas ainda nao tem campo real mapeado no save; o service retornava valor fixo `100` e nao gravava esse campo. A investigacao de `eK` confirmou energia/condicao, nao moral.
