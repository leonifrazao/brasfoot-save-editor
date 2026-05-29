# Brasfoot Save Editor

Editor desktop para saves `.s22` do Brasfoot.

## Arquitetura

- `domain`: modelos e estado do save.
- `application`: portas de entrada e services de caso de uso.
- `presentation`: presenters e modelos de tela consumidos pela UI desktop.
- `infrastructure`: adapters de arquivo/Kryo e acesso aos objetos do Brasfoot.
- `debug`: ferramentas locais para investigar saves e bytecode.

A interface desktop é Java + Qt Jambi, consumindo os services diretamente. O projeto não expõe API REST e não possui frontend web.

O Qt Jambi precisa usar a mesma versão major/minor/patch da Qt instalada no sistema. Este projeto está alinhado com Qt `6.10.2`.

## Fluxo De Save

- Abre um save local por caminho.
- Mantém o save atual em memória simples.
- Permite editar times, jogadores, técnicos e tabelas de ligas/campeonatos pela UI Qt.
- Ao salvar, cria uma cópia nova no mesmo diretório do arquivo original com nome aleatório.
- O arquivo original não é sobrescrito.

## Build E Testes

```bash
mvn test
```

Para abrir a aplicação Qt:

```bash
mvn spring-boot:run
```

Se o ambiente local não tiver Maven instalado:

```bash
nix-shell -p maven --run "mvn test"
```
