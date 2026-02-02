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
    public static final String TEAM_ID = "na"; // int
    public static final String TEAM_PLAYERS = "nd"; // List<Player>
    public static final String TEAM_MONEY = "nb"; // long
    public static final String TEAM_REPUTATION = "nc"; // int
    public static final String TEAM_COUNTRY = "pais"; // int
    public static final String TEAM_STADIUM_CAPACITY = "hr"; // int?
    public static final String TEAM_DIVISION = "divisao"; // int

    // --- Player (best.F) ---
    public static final String PLAYER_NAME = "dm"; // String
    public static final String PLAYER_ID = "bW"; // int (Team ID ref?)
    public static final String PLAYER_AGE = "em"; // int
    public static final String PLAYER_OVERALL = "eq"; // int (Força)

    // CORRECTION: 'en' is POSITION, not SKILL!
    // 0=Gol, 1=Lat, 2=Zag, 3=Mei, 4=Ata
    public static final String PLAYER_POSITION = "en";

    public static final String PLAYER_SIDE = "ex"; // int (0=Dir?, 1=Esq?)

    public static final String PLAYER_ENERGY = "ep"; // int (0-100)
    public static final String PLAYER_SPEED = "eo"; // int
    public static final String PLAYER_SHOT = "er"; // int
    public static final String PLAYER_PASS = "es"; // int
    public static final String PLAYER_HEADING = "et"; // int
    public static final String PLAYER_TACKLING = "eu"; // int
    public static final String PLAYER_CONTRACT_END = "eJ"; // long (Timestamp)

    // Disciplinary (Cards/Suspensions)
    public static final String CARD_YELLOW_1 = "eA";
    public static final String CARD_YELLOW_2 = "eB";
    public static final String CARD_RED = "eC";
    // ... possibly more disciplinary fields eD..eG, eR

    // --- Manager ---
    public static final String MANAGER_NAME = "dm"; // String
    public static final String MANAGER_IS_HUMAN = "mW"; // boolean
    public static final String MANAGER_CONFIDENCE_BOARD = "of"; // int
    public static final String MANAGER_CONFIDENCE_FANS = "og"; // int

}
