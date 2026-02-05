<a id="readme-top"></a>

<!-- PROJECT SHIELDS -->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![AGPL License][license-shield]][license-url]

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/leonifrazao/brasfoot-save-editor">
    <h1>Brasfoot Save Editor</h1>
  </a>

  <h3 align="center">Editor Interativo de Saves do Brasfoot</h3>

  <p align="center">
    Editor de linha de comando (CLI) avançado para visualizar e modificar arquivos de save (.s22) do Brasfoot
    <br />
    <a href="https://github.com/leonifrazao/brasfoot-save-editor"><strong>Explore a documentação »</strong></a>
    <br />
    <br />
    <a href="https://github.com/leonifrazao/brasfoot-save-editor/releases">Ver Releases</a>
    ·
    <a href="https://github.com/leonifrazao/brasfoot-save-editor/issues/new?labels=bug&template=bug-report---.md">Reportar Bug</a>
    ·
    <a href="https://github.com/leonifrazao/brasfoot-save-editor/issues/new?labels=enhancement&template=feature-request---.md">Solicitar Funcionalidade</a>
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Índice</summary>
  <ol>
    <li>
      <a href="#sobre-o-projeto">Sobre o Projeto</a>
      <ul>
        <li><a href="#construído-com">Construído Com</a></li>
      </ul>
    </li>
    <li>
      <a href="#começando">Começando</a>
      <ul>
        <li><a href="#pré-requisitos">Pré-requisitos</a></li>
        <li><a href="#instalação">Instalação</a></li>
      </ul>
    </li>
    <li><a href="#uso">Uso</a></li>
    <li><a href="#funcionalidades">Funcionalidades</a></li>
    <li><a href="#comandos">Referência de Comandos</a></li>
    <li><a href="#exemplo-prático">Exemplo Prático</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contribuindo">Contribuindo</a></li>
    <li><a href="#licença">Licença</a></li>
    <li><a href="#contato">Contato</a></li>
    <li><a href="#agradecimentos">Agradecimentos</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->
## Sobre o Projeto

O **Brasfoot Save Editor** é um editor de linha de comando (CLI) avançado e interativo para visualizar e modificar arquivos de save (`.s22`) do jogo Brasfoot. Construído em Java e utilizando a biblioteca Kryo para deserialização e serialização de dados, a ferramenta permite manipulação profunda e precisa da estrutura dos saves.

Projetado para ser robusto, rápido e amigável, o editor oferece uma interface CLI colorida, comandos intuitivos e funcionalidades de segurança como backups automáticos, tornando-o acessível mesmo para usuários com menos experiência técnica.

### Por que usar o Brasfoot Save Editor?

* 🎨 **Interface Amigável**: CLI colorido que organiza informações e melhora a legibilidade
* 💾 **Segurança de Dados**: Backups automáticos garantem que você nunca perca seus saves originais
* 🔍 **Navegação Intuitiva**: Explore a estrutura complexa de dados de forma hierárquica
* ⚡ **Performance**: Processamento rápido e eficiente de grandes quantidades de dados
* 🎯 **Precisão**: Edição tanto de alto nível (jogadores, times) quanto de baixo nível (campos individuais)

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

### Construído Com

* [![Java][Java]][Java-url]
* [![Maven][Maven]][Maven-url]

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

<!-- GETTING STARTED -->
## Começando

Para começar a usar o Brasfoot Save Editor, siga estas etapas simples.

### Pré-requisitos

* **Java Development Kit (JDK)**: Versão 8 ou superior
  ```sh
  java -version
  ```

### Instalação

#### Método 1: Download do Release

1. Baixe a versão mais recente do `editor-final.jar` na página de [Releases](https://github.com/leonifrazao/brasfoot-save-editor/releases)

2. Execute o arquivo JAR
   ```sh
   java -jar editor-final.jar
   ```

#### Método 2: Compilar do Código Fonte

1. Clone o repositório
   ```sh
   git clone https://github.com/leonifrazao/brasfoot-save-editor.git
   ```

2. Navegue até o diretório do projeto
   ```sh
   cd brasfoot-save-editor
   ```

3. Compile o projeto usando o script apropriado

   **No Windows:**
   ```sh
   .\build.bat
   ```

   **No Linux/macOS:**
   ```sh
   sh ./build.sh
   ```

4. Execute o editor
   ```sh
   java -jar editor-final.jar
   ```

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

<!-- USAGE -->
## Uso

### Iniciando o Editor

Ao executar o editor, ele automaticamente procurará por arquivos `.s22` no diretório atual e solicitará que você escolha um para editar.

```sh
java -jar editor-final.jar
```

### Fluxo Básico

1. **Selecione o Save**: Escolha o número do arquivo de save na lista apresentada
2. **Navegue pela Estrutura**: Use comandos como `entrar`, `item`, `ver` para explorar os dados
3. **Faça Modificações**: Utilize `set`, `editarjogador`, `editartime` para alterar dados
4. **Salve as Alterações**: Use o comando `salvar` para criar um novo arquivo modificado

### Comandos Essenciais

```sh
# Ver conteúdo atual
ver

# Entrar em um campo
entrar ag

# Editar um jogador
editarjogador Pelé; 25; 99

# Salvar modificações
salvar meu_save_editado.s22
```

_Para documentação completa dos comandos, consulte a seção [Referência de Comandos](#comandos)_

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

<!-- FEATURES -->
## Funcionalidades

- [x] 🎨 **Interface CLI Colorida**: Organização visual e melhor legibilidade
- [x] 💾 **Backup Automático**: Cria `.bak` do arquivo original automaticamente
- [x] 🗂️ **Navegação Hierárquica**: Explore dados de forma intuitiva com comandos simples
- [x] 📄 **Visualização Paginada**: Exibe listas gigantes em páginas navegáveis
- [x] 🔍 **Busca Poderosa**: Busca local e global em toda a estrutura do save
- [x] 🗺️ **Mapeamento Rápido**: Gera arquivo de texto com caminhos para objetos específicos
- [x] ⚙️ **Edição de Alto Nível**: Comandos específicos para jogadores e times
- [x] 🔧 **Edição de Baixo Nível**: Modifique qualquer campo individualmente
- [x] 💻 **Multiplataforma**: Scripts de compilação para Windows, Linux e macOS
- [ ] 🖥️ Interface Gráfica (GUI)
- [ ] 📊 Visualização de Estatísticas
- [ ] 🔄 Desfazer/Refazer Alterações

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

<!-- COMMANDS -->
## Comandos

### Referência Completa de Comandos

| Comando | Atalhos | Descrição |
|---------|---------|-----------|
| `ajuda` | - | Mostra a lista completa de comandos disponíveis |
| `ver` | - | Lista os campos do objeto atual e conteúdo paginado |
| `entrar <campo>` | - | Navega dentro do objeto disponível em um campo |
| `item <índice>` | - | Navega para um item específico de lista/array |
| `proxima` | `p` | Avança para a próxima página |
| `anterior` | `a` | Retrocede para a página anterior |
| `voltar` | - | Volta ao objeto anterior na hierarquia |
| `topo` | - | Retorna ao objeto raiz do save |
| `buscar <termo>` | - | Busca um termo a partir do objeto atual |
| `busca-global <termo>` | - | Busca um termo em todo o arquivo |
| `set <campo> = <valor>` | - | Modifica o valor de um campo |
| `mapear <arq>; <termo>` | - | Mapeia todos os objetos que contêm o termo |
| `editarjogador <n>;<i>;<o>` | - | Edita idade e overall de um jogador |
| `editartime <t>;<a>;<v>` | - | Altera atributo de todos jogadores do time |
| `salvar <arquivo.s22>` | - | Salva modificações em novo arquivo |
| `sair` | - | Fecha o editor |

### Exemplos de Comandos

```sh
# Navegação
entrar ag                    # Entra no campo 'ag'
item 10                      # Vai para o item 10 da lista
voltar                       # Volta um nível
topo                         # Volta à raiz

# Busca
buscar Neymar               # Busca local
busca-global Flamengo       # Busca global
mapear jogadores.txt; Messi # Mapeia localizações

# Edição
set eq = 99                 # Define campo eq como 99
editarjogador Romário; 28; 95  # Edita jogador
editartime Corinthians; eq; 90 # Edita time inteiro

# Salvar
salvar brasfoot_modificado.s22
```

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

<!-- EXAMPLE -->
## Exemplo Prático

### Cenário: Aumentar o Overall de um Jogador

Vamos modificar o jogador "Zico" para ter 99 de overall:

#### Passo 1: Iniciar o Editor
```sh
java -jar editor-final.jar
```

#### Passo 2: Selecionar o Save
```
Arquivos .s22 encontrados:
[1] meu_save.s22
[2] campeonato_2024.s22

Escolha um arquivo: 1
```

#### Passo 3: Localizar o Jogador
```sh
[raiz] > mapear zico.txt; Zico
```

Abra o arquivo `zico.txt` gerado. Ele mostrará algo como: `raiz.ag[42]`

#### Passo 4: Editar o Jogador
```sh
[raiz] > editarjogador Zico; 25; 99

✓ Jogador 'Zico' modificado com sucesso!
  - Idade: 25
  - Overall: 99
```

#### Passo 5: Salvar as Alterações
```sh
[raiz] > salvar brasfoot_zico_99.s22

✓ Save salvo com sucesso em: brasfoot_zico_99.s22
```

Pronto! Seu save modificado está pronto para uso no Brasfoot.

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

<!-- ROADMAP -->
## Roadmap

- [x] CLI colorido e interativo
- [x] Sistema de navegação hierárquica
- [x] Comandos de edição de alto nível
- [x] Sistema de busca e mapeamento
- [x] Backup automático
- [ ] Interface gráfica (GUI)
- [ ] Exportação de estatísticas
- [ ] Sistema de plugins
- [ ] Suporte para múltiplos saves simultâneos
- [ ] Editor visual de formações táticas
- [ ] Sistema de templates/presets
- [ ] Comparação entre saves
- [ ] Histórico de modificações (undo/redo)

Veja as [issues abertas](https://github.com/leonifrazao/brasfoot-save-editor/issues) para uma lista completa de funcionalidades propostas e problemas conhecidos.

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

<!-- CONTRIBUTING -->
## Contribuindo

As contribuições são o que tornam a comunidade open source um lugar incrível para aprender, inspirar e criar. Qualquer contribuição que você fizer será **muito apreciada**.

Se você tiver uma sugestão para melhorar o projeto, faça um fork do repositório e crie um pull request. Você também pode simplesmente abrir uma issue com a tag "enhancement".
Não se esqueça de dar uma estrela ao projeto! Obrigado novamente!

1. Faça um Fork do Projeto
2. Crie sua Branch de Funcionalidade (`git checkout -b feature/NovaFuncionalidade`)
3. Commit suas Mudanças (`git commit -m 'Adiciona NovaFuncionalidade'`)
4. Push para a Branch (`git push origin feature/NovaFuncionalidade`)
5. Abra um Pull Request

### Principais Contribuidores

<a href="https://github.com/leonifrazao/brasfoot-save-editor/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=leonifrazao/brasfoot-save-editor" alt="contrib.rocks image" />
</a>

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

<!-- LICENSE -->
## Licença

Distribuído sob a Licença AGPL-3.0. Veja `LICENSE` para mais informações.

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

<!-- CONTACT -->
## Contato

Leoni Frazão - [@leonifrazao](https://github.com/leonifrazao)

Link do Projeto: [https://github.com/leonifrazao/brasfoot-save-editor](https://github.com/leonifrazao/brasfoot-save-editor)

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

<!-- ACKNOWLEDGMENTS -->
## Agradecimentos

Recursos e ferramentas que tornaram este projeto possível:

* [Java](https://www.oracle.com/java/)
* [Kryo](https://github.com/EsotericSoftware/kryo)
* [Maven](https://maven.apache.org/)
* [Brasfoot](http://www.brasfoot.com/)
* [Choose an Open Source License](https://choosealicense.com)
* [Img Shields](https://shields.io)
* [GitHub Pages](https://pages.github.com)

<p align="right">(<a href="#readme-top">voltar ao topo</a>)</p>

---

## 📁 Estrutura do Projeto

```
brasfoot-save-editor/
├── lib/                    # Dependências JAR necessárias
├── src/main/              # Código-fonte principal
├── presets/               # Configurações predefinidas
├── build.bat              # Script de compilação (Windows)
├── build.sh               # Script de compilação (Linux/macOS)
├── pom.xml                # Configuração Maven
├── config.properties      # Arquivo de configuração
└── shell.nix             # Configuração para ambientes Nix
```

---

<div align="center">

### ⚽ Feito para a comunidade Brasfoot

*Edite seus saves com precisão e segurança*

**[⬆ Voltar ao topo](#readme-top)**

</div>

<!-- MARKDOWN LINKS & IMAGES -->
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
[Java]: https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[Java-url]: https://www.oracle.com/java/
[Maven]: https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white
[Maven-url]: https://maven.apache.org/
