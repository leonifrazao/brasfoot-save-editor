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

  <h3 align="center">Interactive Brasfoot Save Editor</h3>

  <p align="center">
    Advanced command-line editor (CLI) for viewing and modifying Brasfoot save files (.s22)
    <br />
    <a href="https://github.com/leonifrazao/brasfoot-save-editor"><strong>Explore the documentation »</strong></a>
    <br />
    <br />
    <a href="https://github.com/leonifrazao/brasfoot-save-editor/releases">View Releases</a>
    ·
    <a href="https://github.com/leonifrazao/brasfoot-save-editor/issues/new?labels=bug&template=bug-report---.md">Report Bug</a>
    ·
    <a href="https://github.com/leonifrazao/brasfoot-save-editor/issues/new?labels=enhancement&template=feature-request---.md">Request Feature</a>
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#features">Features</a></li>
    <li><a href="#commands">Command Reference</a></li>
    <li><a href="#practical-example">Practical Example</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->
## About The Project

**Brasfoot Save Editor** is an advanced and interactive command-line editor (CLI) for viewing and modifying Brasfoot game save files (`.s22`). Built in Java and using the Kryo library for data deserialization and serialization, the tool enables deep and precise manipulation of save file structures.

Designed to be robust, fast, and user-friendly, the editor offers a colorized CLI interface, intuitive commands, and security features like automatic backups, making it accessible even for users with less technical experience.

### Why use Brasfoot Save Editor?

* **User-Friendly Interface**: Colorized CLI that organizes information and improves readability
* **Data Security**: Automatic backups ensure you never lose your original saves
* **Intuitive Navigation**: Explore complex data structures hierarchically
* **Performance**: Fast and efficient processing of large amounts of data
* **Precision**: Editing at both high level (players, teams) and low level (individual fields)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Built With

* [![Java][Java]][Java-url]
* [![Maven][Maven]][Maven-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->
## Getting Started

To start using Brasfoot Save Editor, follow these simple steps.

### Prerequisites

* **Java Development Kit (JDK)**: Version 8 or higher
  ```sh
  java -version
  ```

### Installation

#### Method 1: Download Release

1. Download the latest version of `editor-final.jar` from the [Releases](https://github.com/leonifrazao/brasfoot-save-editor/releases) page

2. Run the JAR file
   ```sh
   java -jar editor-final.jar
   ```

#### Method 2: Compile from Source Code

1. Clone the repository
   ```sh
   git clone https://github.com/leonifrazao/brasfoot-save-editor.git
   ```

2. Navigate to the project directory
   ```sh
   cd brasfoot-save-editor
   ```

3. Compile the project using the appropriate script

   **On Windows:**
   ```sh
   .\build.bat
   ```

   **On Linux/macOS:**
   ```sh
   sh ./build.sh
   ```

4. Run the editor
   ```sh
   java -jar editor-final.jar
   ```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- USAGE -->
## Usage

### Starting the Editor

When running the editor, it will automatically search for `.s22` files in the current directory and prompt you to choose one to edit.

```sh
java -jar editor-final.jar
```

### Basic Workflow

1. **Select the Save**: Choose the number of the save file from the presented list
2. **Navigate the Structure**: Use commands like `enter`, `item`, `view` to explore data
3. **Make Modifications**: Use `set`, `editplayer`, `editteam` to change data
4. **Save Changes**: Use the `save` command to create a new modified file

### Essential Commands

```sh
# View current content
view

# Enter a field
enter ag

# Edit a player
editplayer Pelé; 25; 99

# Save modifications
save my_edited_save.s22
```

_For complete command documentation, see the [Command Reference](#commands) section_

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- FEATURES -->
## Features

- [x] **Colorized CLI Interface**: Visual organization and better readability
- [x] **Automatic Backup**: Creates `.bak` of original file automatically
- [x] **Hierarchical Navigation**: Explore data intuitively with simple commands
- [x] **Paginated Display**: Shows large lists in navigable pages
- [x] **Powerful Search**: Local and global search throughout the save structure
- [x] **Quick Mapping**: Generates text file with paths to specific objects
- [x] **High-Level Editing**: Specific commands for players and teams
- [x] **Low-Level Editing**: Modify any field individually
- [x] **Cross-Platform**: Compilation scripts for Windows, Linux, and macOS
- [ ] Graphical Interface (GUI)
- [ ] Statistics Visualization
- [ ] Undo/Redo Changes

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- COMMANDS -->
## Commands

### Complete Command Reference

| Command | Shortcuts | Description |
|---------|-----------|-------------|
| `help` | - | Shows complete list of available commands |
| `view` | - | Lists fields of current object and paginated content |
| `enter <field>` | - | Navigates into the object available in a field |
| `item <index>` | - | Navigates to a specific list/array item |
| `next` | `n` | Advances to next page |
| `previous` | `p` | Returns to previous page |
| `back` | - | Returns to previous object in hierarchy |
| `top` | - | Returns to root object of save |
| `search <term>` | - | Searches for a term from current object |
| `global-search <term>` | - | Searches for a term in entire file |
| `set <field> = <value>` | - | Modifies a field's value |
| `map <file>; <term>` | - | Maps all objects containing the term |
| `editplayer <n>;<a>;<o>` | - | Edits player's age and overall |
| `editteam <t>;<a>;<v>` | - | Changes attribute for all team players |
| `save <file.s22>` | - | Saves modifications to new file |
| `exit` | - | Closes the editor |

### Command Examples

```sh
# Navigation
enter ag                    # Enters the 'ag' field
item 10                     # Goes to item 10 of the list
back                        # Goes back one level
top                         # Returns to root

# Search
search Neymar               # Local search
global-search Flamengo      # Global search
map players.txt; Messi      # Maps locations

# Editing
set eq = 99                 # Sets eq field to 99
editplayer Romário; 28; 95  # Edits player
editteam Corinthians; eq; 90 # Edits entire team

# Save
save brasfoot_modified.s22
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- EXAMPLE -->
## Practical Example

### Scenario: Increase a Player's Overall

Let's modify player "Zico" to have 99 overall:

#### Step 1: Start the Editor
```sh
java -jar editor-final.jar
```

#### Step 2: Select the Save
```
.s22 files found:
[1] my_save.s22
[2] championship_2024.s22

Choose a file: 1
```

#### Step 3: Locate the Player
```sh
[root] > map zico.txt; Zico
```

Open the generated `zico.txt` file. It will show something like: `root.ag[42]`

#### Step 4: Edit the Player
```sh
[root] > editplayer Zico; 25; 99

✓ Player 'Zico' successfully modified!
  - Age: 25
  - Overall: 99
```

#### Step 5: Save Changes
```sh
[root] > save brasfoot_zico_99.s22

✓ Save successfully saved in: brasfoot_zico_99.s22
```

Done! Your modified save is ready to use in Brasfoot.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- ROADMAP -->
## Roadmap

- [x] Colorized and interactive CLI
- [x] Hierarchical navigation system
- [x] High-level editing commands
- [x] Search and mapping system
- [x] Automatic backup
- [ ] Graphical interface (GUI)
- [ ] Statistics export
- [ ] Plugin system
- [ ] Support for multiple simultaneous saves
- [ ] Visual tactical formation editor
- [ ] Template/preset system
- [ ] Save comparison
- [ ] Modification history (undo/redo)

See the [open issues](https://github.com/leonifrazao/brasfoot-save-editor/issues) for a complete list of proposed features and known issues.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion to improve the project, please fork the repository and create a pull request. You can also simply open an issue with the "enhancement" tag.
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Top Contributors

<a href="https://github.com/leonifrazao/brasfoot-save-editor/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=leonifrazao/brasfoot-save-editor" alt="contrib.rocks image" />
</a>

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- LICENSE -->
## License

Distributed under the AGPL-3.0 License. See `LICENSE` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTACT -->
## Contact

Leoni Frazão - [@leonifrazao](https://github.com/leonifrazao)

Project Link: [https://github.com/leonifrazao/brasfoot-save-editor](https://github.com/leonifrazao/brasfoot-save-editor)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

Resources and tools that made this project possible:

* [Java](https://www.oracle.com/java/)
* [Kryo](https://github.com/EsotericSoftware/kryo)
* [Maven](https://maven.apache.org/)
* [Brasfoot](http://www.brasfoot.com/)
* [Choose an Open Source License](https://choosealicense.com)
* [Img Shields](https://shields.io)
* [GitHub Pages](https://pages.github.com)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Project Structure

```
brasfoot-save-editor/
├── lib/                    # Required JAR dependencies
├── src/main/              # Main source code
├── presets/               # Predefined configurations
├── build.bat              # Compilation script (Windows)
├── build.sh               # Compilation script (Linux/macOS)
├── pom.xml                # Maven configuration
├── config.properties      # Configuration file
└── shell.nix             # Configuration for Nix environments
```

---

<div align="center">

### Made for the Brasfoot community

*Edit your saves with precision and security*

**[Back to top](#readme-top)**

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
