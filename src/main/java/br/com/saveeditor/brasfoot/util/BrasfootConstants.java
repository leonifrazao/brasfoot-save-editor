package br.com.saveeditor.brasfoot.util;

/**
 * Constants for obfuscated Brasfoot fields.
 * This class maps the obfuscated field names (e.g. "dm", "aj") to readable
 * constants.
 */
public class BrasfootConstants {

    // --- Game Root ---
    public static final String TEAMS_LIST = "aj"; // List<Team>
    public static final String HUMAN_MANAGERS_LIST = "al"; // List<Manager>

    // --- Team (best.ah) ---
    public static final String TEAM_NAME = "dm"; // String
    public static final String TEAM_ALIAS = "mV"; // String; getter jY()
    public static final String TEAM_ID = "mU"; // int; internal team id used by best.ah.lk()
    public static final String TEAM_MANAGER_ID = "na"; // int; current manager id stored on team
    public static final String TEAM_IS_HUMAN = "mW"; // Boolean; team controlled by human
    public static final String TEAM_MANAGER_REFERENCE = "mZ"; // transient best.al; current manager reference
    public static final String TEAM_PLAYERS = "nd"; // List<Player>
    public static final String TEAM_MONEY = "nb"; // long
    public static final String TEAM_REPUTATION = "nc"; // int
    public static final String TEAM_COUNTRY = "pais"; // int
    public static final String TEAM_STADIUM_CAPACITY = "hr"; // int?
    public static final String TEAM_STADIUM = "dH"; // Stadium object
    public static final String STADIUM_NAME = "dm"; // String - Inside Stadium
    public static final String STADIUM_SECTORS = "dn"; // int[] (Geral, Arq, Cad, Cam) - Inside Stadium
    public static final String TEAM_DIVISION = "divisao"; // int
    public static final String TEAM_LEVEL = "hA"; // int; getter getNivel()
    public static final String TEAM_TACTICS = "nq"; // int[]; kj()/k(int[])

    // --- Player (best.F) ---
    public static final String PLAYER_NAME = "dm"; // String
    public static final String PLAYER_ID = "bW"; // int (Team ID ref?)
    public static final String PLAYER_AGE = "em"; // int
    public static final String PLAYER_OVERALL = "eq"; // int (Força)
    public static final String PLAYER_SALARY = "et"; // int (Salário atual; getter obfuscado best.F.fj())

    // CORRECTION: 'en' is POSITION, not SKILL!
    // 0=Gol, 1=Lat, 2=Zag, 3=Mei, 4=Ata
    public static final String PLAYER_POSITION = "en";

    public static final String PLAYER_SIDE = "ex"; // int (0=Dir?, 1=Esq?)

    public static final String PLAYER_ENERGY = "eK"; // int (0-100); getter best.F.fp(), shown as Energia in original table
    public static final String PLAYER_CONTRACT_END = "eJ"; // long (Timestamp)
    public static final String PLAYER_CHARACTERISTIC_1 = "ey"; // int; getCr1()/setCr1(int)
    public static final String PLAYER_CHARACTERISTIC_2 = "ez"; // int; getCr2()/setCr2(int)
    public static final String PLAYER_STAR_LOCAL = "ek"; // boolean
    public static final String PLAYER_STAR_GLOBAL = "el"; // boolean
    public static final String PLAYER_SKILL_GOALKEEPING = "eA"; // int (0-100); getter best.F.gK()
    public static final String PLAYER_SKILL_SPEED = "eB"; // int (0-100); getter best.F.gJ(), shown as Velocidade/Vel
    public static final String PLAYER_SKILL_TECHNIQUE = "eC"; // int (0-100); getter best.F.gL()
    public static final String PLAYER_SKILL_PASSING = "eD"; // int (0-100); getter best.F.gM()
    public static final String PLAYER_SKILL_TACKLING = "eE"; // int (0-100); getter best.F.gN(), shown as Desarme/Des
    public static final String PLAYER_SKILL_PLAYMAKING = "eF"; // int (0-100); getter best.F.gO(), shown as Armacao/Arm
    public static final String PLAYER_PAIS = "pais"; // int (ID do pais, ver Country enum)

    public static final String PLAYER_SKILL_FINISHING = "eG"; // int (0-100); getter best.F.gP()


    // --- Manager ---
    public static final String MANAGER_NAME = "dm"; // String
    public static final String MANAGER_IS_HUMAN = "mW"; // boolean
    public static final String MANAGER_CONFIDENCE_BOARD = "of"; // int
    public static final String MANAGER_CONFIDENCE_FANS = "og"; // int
    public static final String MANAGER_ID = "nU"; // int; manager id used by best.al.lT()
    public static final String MANAGER_CURRENT_TEAM = "nV"; // transient best.ah; current team reference
    public static final String MANAGER_CURRENT_TEAM_ID = "bW"; // int; current team id cache used by best.al.fg()
    public static final String MANAGER_PREVIOUS_TEAM = "nW"; // transient best.ah; previous team reference
    public static final String MANAGER_PREVIOUS_TEAM_ID = "nX"; // int; previous team id cache
    public static final String MANAGER_TROPHIES = "cA"; // ArrayList<best.ao>; manager title history returned by best.al.cT()

    // --- Manager trophy/history item (best.ao) ---
    public static final String MANAGER_TROPHY_YEAR = "ae"; // int; season/year index
    public static final String MANAGER_TROPHY_COMPETITION_TYPE = "w"; // int; best.at.b()
    public static final String MANAGER_TROPHY_VARIANT = "dz"; // int; country/division/variant depending on competition type
    public static final String MANAGER_TROPHY_TEAM_ID = "bW"; // int; winning/current team id
    public static final String MANAGER_TROPHY_COMPETITION_REFERENCE = "Y"; // best.at; optional competition reference
    public static final String ROOT_SCHEDULES = "as"; // ArrayList<best.a>; returned by best.f.R()
    public static final String SCHEDULE_COMPETITIONS = "u"; // ArrayList<best.at>; returned by best.a.t()
    public static final String COMPETITION_NAME = "dm"; // String; best.at.getNome()
    public static final String COMPETITION_TYPE = "tR"; // int; best.at.b()
    public static final String COMPETITION_VARIANT = "dz"; // int; best.at.gg()/el()

    // --- Human-controlled teams ---
    public static final String HUMAN_TEAMS_LIST = "ak"; // List<Team>; returned by best.f.aN()

    // --- Country/Division/League ---
    public static final String ROOT_PRIMARY_COUNTRIES = "ao";
    public static final String ROOT_SECONDARY_COUNTRIES = "ap";
    public static final String COUNTRY_DIVISIONS = "ds";
    public static final String COUNTRY_NAME = "hB"; // String; getter jf()
    public static final String COUNTRY_LEVEL = "hA"; // int; getter getNivel()

    // --- Division (f.B) ---
    public static final String DIVISION_MAIN_LEAGUE = "YL";
    public static final String DIVISION_SECONDARY_LEAGUE = "ZU";
    public static final String DIVISION_NUMBER = "divisao"; // int
    public static final String DIVISION_RELEGATION_SPOTS = "nRebaixados"; // int
    public static final String DIVISION_DIRECT_RELEGATIONS = "rebaixadosDireto"; // int
    public static final String DIVISION_PROMOTION_PLAYOFF_SPOTS = "vagasSobemPeloMataMata"; // int

    // --- League (f.s) ---
    public static final String LEAGUE_TEAMS = "Zb";
    public static final String LEAGUE_NAME = "nomeLiga";
    public static final String LEAGUE_DIVISION_NAME = "nomeDivisao";
    public static final String LEAGUE_FALLBACK_NAME = "nome";
    public static final String LEAGUE_NUMBER_OF_TEAMS = "nTimes"; // int
    public static final String LEAGUE_DOUBLE_ROUND = "doisTurnos"; // boolean
    public static final String LEAGUE_GAMES_WITHIN_GROUP = "jogosDentroGrupo"; // boolean
    public static final String LEAGUE_NUMBER_OF_ROUNDS = "numeroTurnos"; // int
    public static final String LEAGUE_TIEBREAKER = "desempateEstadual"; // int
    public static final String LEAGUE_BEST_THIRDS = "melhoresTerceiros"; // boolean

    // --- League table stats (best.ak) ---
    public static final String LEAGUE_STATS_POINTS = "nT";
    public static final String LEAGUE_STATS_PLAYED = "T";
    public static final String LEAGUE_STATS_WINS = "bX";
    public static final String LEAGUE_STATS_LOSSES = "d";
    public static final String LEAGUE_STATS_GOALS_FOR = "nK";
    public static final String LEAGUE_STATS_GOALS_AGAINST = "nL";

}
