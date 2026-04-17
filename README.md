<a id="readme-top"></a>

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![AGPL License][license-shield]][license-url]

<div align="center">
  <h1>Brasfoot Save Editor</h1>
  <h3>Editor de saves .s22 com API REST + interface web</h3>

  <p>
    Projeto para carregar, editar e exportar arquivos de save do Brasfoot em uma sessao em memoria.
    <br />
    <a href="https://github.com/leonifrazao/brasfoot-save-editor"><strong>Repositorio no GitHub</strong></a>
  </p>
</div>

## Sumario

- [Sobre o projeto](#sobre-o-projeto)
- [Arquitetura](#arquitetura)
- [Como o save foi mapeado](#como-o-save-foi-mapeado)
- [Funcionalidades](#funcionalidades)
- [Stack](#stack)
- [Como executar localmente](#como-executar-localmente)
- [Fluxo de uso](#fluxo-de-uso)
- [API principal](#api-principal)
- [Exemplo rapido com curl](#exemplo-rapido-com-curl)
- [Testes e qualidade](#testes-e-qualidade)
- [Estrutura do repositorio](#estrutura-do-repositorio)
- [Contribuicao](#contribuicao)
- [Licenca](#licenca)

## Sobre o projeto

O **Brasfoot Save Editor** evoluiu para um produto web com duas partes:

- **Backend Spring Boot** que carrega o arquivo `.s22`, expoe endpoints REST e gerencia sessao em memoria.
- **Frontend Next.js** para upload, navegacao de workspace e edicao de entidades.

### Estado atual

- O fluxo principal e: **upload -> editar -> download**.
- A sessao e mantida em cache por ate **1 hora**.
- Ao fazer download do save, a sessao e marcada como encerrada (nao reutilizavel).
- O frontend foi desenhado para **desktop/tablet** (largura >= 768px).
- Upload limitado a **500 MB**.

## Arquitetura

### Backend (Spring Boot)

- API versionada em `/api/v1`.
- Organizacao orientada a portas e adaptadores (hexagonal).
- Persistencia de sessao em cache Caffeine:
  - sessao ativa: 1 hora
  - tombstone de sessao expirada/deletada: 24 horas
- Leitura/escrita de save com Kryo.

### Frontend (Next.js App Router)

- Tela de intake para upload do `.s22`.
- Workspace com secoes: **Overview**, **Teams**, **Players**, **Managers**.
- Proxy interno em `/api/brasfoot/*` para o backend.
- Edicao individual e em lote nas entidades suportadas.
- Atalho de navegacao com command palette (`Ctrl/Cmd + K`).

## Como o save foi mapeado

O arquivo `.s22` do Brasfoot e um objeto Java serializado com **Kryo**, uma biblioteca de serializacao binaria de alta performance. O jogo aplica ofuscacao nos nomes dos campos — atributos que semanticamente sao `money`, `overall` ou `reputation` aparecem no binario como `nb`, `eq`, `nc` e similares, sem documentacao publica de nenhum tipo.

O processo de mapeamento foi feito por engenharia reversa direta: inspecionar o objeto deserializado em runtime, correlacionar os valores dos campos ofuscados com o comportamento observado no jogo e validar cada campo manualmente contra saves reais. O resultado esta centralizado em `BrasfootConstants.java`, que mapeia cada chave ofuscada para uma constante legivel com tipo e descricao. Campos identificados mas ainda nao validados foram isolados em `BrasfootUnverifiedConstants.java` para evitar uso acidental em codigo de producao.

## Funcionalidades

- Upload de save `.s22` e abertura de sessao.
- Download do save editado.
- Visualizacao e edicao de:
  - times (dinheiro e reputacao)
  - jogadores (idade, overall, posicao, energia, moral, estrelas)
  - tecnicos (nome, confianca da diretoria e torcida)
- Atualizacao em lote por entidade.
- Endpoint de comandos mistos em lote (`/commands/batch`).
- Mensagens de erro padronizadas via `ProblemDetail`.

## Stack

- Java 17+
- Spring Boot 3.2
- Spring Web + Validation + springdoc-openapi
- Caffeine Cache
- Kryo
- Next.js 16 + React 19 + TypeScript
- Tailwind CSS

## Como executar localmente

Você pode executar o projeto de três maneiras diferentes, dependendo da sua preferência:

### Opcao 1: Usando Docker Compose (Recomendado)

A maneira mais rapida de executar a aplicacao sem instalar as ferramentas localmente. Pre-requisito: ter o Docker instalado.

1. Clone o repositorio:
```bash
git clone https://github.com/leonifrazao/brasfoot-save-editor.git
cd brasfoot-save-editor
```
2. Suba todos os servicos de uma vez em background:
```bash
docker compose up -d
```
3. Acesse:
   - Frontend Web: `http://localhost:3000`
   - Backend API e Swagger: `http://localhost:8080/swagger-ui/index.html`

### Opcao 2: Usando Nix-shell

Se voce usa o gerenciador de pacotes **Nix**, o repositorio inclui um `shell.nix` que ja configura o ecossistema exato do projeto (Java 17, Node 20+, Maven, Docker, etc).

1. Na pasta do projeto clonado, ative o ambiente virtual:
```bash
nix-shell
```
2. Pronto! Voce tera um painel interativo confirmando todas as versoes e podera executar os comandos manuais para backend (`mvn spring-boot:run`) ou frontend (`cd frontend && npm run dev`) tudo no mesmo shell homogeneo.

### Opcao 3: Execucao Manual

#### Pre-requisitos

- Java 17 ou superior
- Maven 3.9+
- Node.js 20+
- npm 10+

#### 1. Clone o repositorio

```bash
git clone https://github.com/leonifrazao/brasfoot-save-editor.git
cd brasfoot-save-editor
```

#### 2. (Opcional) instalar dependencia proprietaria local

Se o Maven reclamar de `com.brasfoot:brasfoot-game:1.0`, instale o jar que ja esta no repositorio:

```bash
mvn install:install-file \
  -Dfile=lib/brasfoot.jar \
  -DgroupId=com.brasfoot \
  -DartifactId=brasfoot-game \
  -Dversion=1.0 \
  -Dpackaging=jar
```

#### 3. Subir o backend (porta 8080)

```bash
mvn spring-boot:run
```

Swagger/OpenAPI:

- http://localhost:8080/swagger-ui/index.html
- http://localhost:8080/v3/api-docs

#### 4. Subir o frontend (porta 3000)

Em outro terminal:

```bash
cd frontend
npm install
```

Crie um arquivo `frontend/.env.local` (opcional, recomendado):

```env
BRASFOOT_API_BASE_URL=http://localhost:8080
```

Depois execute:

```bash
npm run dev
```

Aplicacao web:

- http://localhost:3000

## Fluxo de uso

1. Abra a home do frontend em `http://localhost:3000`.
2. Envie um arquivo `.s22`.
3. Navegue pelo workspace (Overview, Teams, Players, Managers).
4. Salve as alteracoes que quiser por entidade ou em lote.
5. Clique em **Download save** para exportar o arquivo editado.

> Observacao: apos o download, a sessao e invalidada no backend.

## API principal

| Metodo | Rota | Descricao |
|---|---|---|
| `POST` | `/api/v1/sessions` | Upload do save (`multipart/form-data`, campo `file`) |
| `GET` | `/api/v1/sessions/{sessionId}/download` | Download do save editado e encerramento da sessao |
| `GET` | `/api/v1/sessions/{sessionId}/teams` | Lista times |
| `PATCH` | `/api/v1/sessions/{sessionId}/teams/{teamId}` | Atualiza um time |
| `PATCH` | `/api/v1/sessions/{sessionId}/teams/batch` | Atualiza varios times |
| `GET` | `/api/v1/sessions/{sessionId}/teams/{teamId}/players` | Lista jogadores do time |
| `PATCH` | `/api/v1/sessions/{sessionId}/teams/{teamId}/players/{playerId}` | Atualiza um jogador |
| `PATCH` | `/api/v1/sessions/{sessionId}/teams/{teamId}/players/batch` | Atualiza varios jogadores |
| `GET` | `/api/v1/sessions/{sessionId}/managers` | Lista tecnicos |
| `PATCH` | `/api/v1/sessions/{sessionId}/managers/{managerId}` | Atualiza um tecnico |
| `PATCH` | `/api/v1/sessions/{sessionId}/managers/batch` | Atualiza varios tecnicos |
| `POST` | `/api/v1/sessions/{sessionId}/commands/batch` | Executa lote misto (`team.update`, `player.update`, `manager.update`) |

### Reputacao de time

Mapeamento utilizado pela API:

- `0`: Municipal
- `1`: Estadual
- `2`: Regional
- `3`: Nacional
- `4`: Continental
- `5`: Mundial

## Exemplo rapido com curl

### 1. Upload do save

```bash
curl -X POST "http://localhost:8080/api/v1/sessions" \
  -F "file=@meu-save.s22"
```

Resposta esperada:

```json
{
  "sessionId": "UUID_DA_SESSAO"
}
```

### 2. Atualizar um time

```bash
curl -X PATCH "http://localhost:8080/api/v1/sessions/UUID_DA_SESSAO/teams/12" \
  -H "Content-Type: application/json" \
  -d '{"money":50000000,"reputation":5}'
```

### 3. Download do save editado

```bash
curl -L "http://localhost:8080/api/v1/sessions/UUID_DA_SESSAO/download" -o save-editado.s22
```

### Exemplo de comando misto em lote

```json
[
  {
    "type": "team.update",
    "teamId": 1,
    "money": 70000000,
    "reputation": 5
  },
  {
    "type": "player.update",
    "teamId": 1,
    "playerId": 8,
    "overall": 99
  },
  {
    "type": "manager.update",
    "managerId": 2,
    "confidenceBoard": 100
  }
]
```

## Testes e qualidade

Backend:

```bash
mvn test
```

Frontend:

```bash
cd frontend
npm run lint
```

## Estrutura do repositorio

```text
brasfoot-save-editor/
├── frontend/                   # Aplicacao Next.js (UI)
│   └── src/
│       ├── app/                # Rotas e pages
│       ├── components/         # Componentes de interface
│       └── lib/                # Cliente API, tipos e rotas
├── lib/                        # JARs locais usados no projeto
├── src/main/java/              # Backend Spring Boot
├── src/main/resources/         # Configuracoes backend
├── src/test/java/              # Testes backend
├── pom.xml                     # Build Maven
└── README.md
```

## Contribuicao

Contribuicoes sao bem-vindas.

1. Faca um fork
2. Crie uma branch (`feature/minha-feature`)
3. Commit suas alteracoes
4. Abra um Pull Request

## Licenca

Distribuido sob a licenca AGPL-3.0. Veja [LICENSE](LICENSE).

<p align="right">(<a href="#readme-top">back to top</a>)</p>

[contributors-shield]: https://img.shields.io/github/contributors/leonifrazao/brasfoot-save-editor.svg?style=for-the-badge
[contributors-url]: https://github.com/leonifrazao/brasfoot-save-editor/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/leonifrazao/brasfoot-save-editor.svg?style=for-the-badge
[forks-url]: https://github.com/leonifrazao/brasfoot-save-editor/network/members
[stars-shield]: https://img.shields.io/github/stars/leonifrazao/brasfoot-save-editor.svg?style=for-the-badge
[stars-url]: https://github.com/leonifrazao/brasfoot-save-editor/stargazers
[issues-shield]: https://img.shields.io/github/issues/leonifrazao/brasfoot-save-editor.svg?style=for-the-badge
[issues-url]: https://github.com/leonifrazao/brasfoot-save-editor/issues
[license-shield]: https://img.shields.io/github/license/leonifrazao/brasfoot-save-editor.svg?style=for-the-badge
[license-url]: https://github.com/leonifrazao/brasfoot-save-editor/blob/master/LICENSE
