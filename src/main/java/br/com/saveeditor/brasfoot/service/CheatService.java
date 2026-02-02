package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ConsoleHelper;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import br.com.saveeditor.brasfoot.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.lang.reflect.Field;

import org.springframework.stereotype.Service;

@Service
public class CheatService {

    private final EditorService editorService;

    public CheatService(EditorService editorService) {
        this.editorService = editorService;
    }

    public void injectMoney(Object team, long amount) {
        try {
            ReflectionUtils.setFieldValue(team, BrasfootConstants.TEAM_MONEY, amount);
            System.out.println(ConsoleHelper.success("Money injected successfully!"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject money: " + e.getMessage());
        }
    }

    public void maxReputation(Object team) {
        try {
            ReflectionUtils.setFieldValue(team, BrasfootConstants.TEAM_REPUTATION, 10000);
            System.out.println(ConsoleHelper.success("Reputation maximized!"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set reputation: " + e.getMessage());
        }
    }

    public void healTeam(Object team) {
        try {
            List<?> players = (List<?>) ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_PLAYERS);
            if (players == null)
                return;

            int count = 0;
            for (Object player : players) {
                ReflectionUtils.setFieldValue(player, BrasfootConstants.PLAYER_ENERGY, 100);
                count++;
            }
            System.out.println(ConsoleHelper.success("Healed " + count + " players!"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to heal team: " + e.getMessage());
        }
    }

    public void superstarPlayer(Object player) {
        try {
            // Set all main attributes to 100
            // eq - Overall
            // en - Skill
            // eo - Speed
            // ep - Energy
            // er - Shot
            // es - Pass
            // et - Heading
            // eu - Tackling

            // Set Overall to 100 (Max strength)
            ReflectionUtils.setFieldValue(player, BrasfootConstants.PLAYER_OVERALL, 100);

            // Age to 18
            ReflectionUtils.setFieldValue(player, BrasfootConstants.PLAYER_AGE, 18);

            System.out.println(ConsoleHelper.success("Player is now a Superstar (Age 18, Overall 100)!"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upgrade player: " + e.getMessage());
        }
    }

    public void maximizeConfidence(Object root) {
        System.out.println(ConsoleHelper.info("Searching for Human Manager to maximize confidence..."));
        try {
            List<?> managers = (List<?>) ReflectionUtils.getFieldValue(root, BrasfootConstants.HUMAN_MANAGERS_LIST);
            if (managers == null) {
                System.out.println(ConsoleHelper.error("No manager list found!"));
                return;
            }

            boolean found = false;
            for (Object mgr : managers) {
                try {
                    Boolean isHuman = (Boolean) ReflectionUtils.getFieldValue(mgr, BrasfootConstants.MANAGER_IS_HUMAN);
                    if (isHuman != null && isHuman) {
                        ReflectionUtils.setFieldValue(mgr, BrasfootConstants.MANAGER_CONFIDENCE_BOARD, 100);
                        ReflectionUtils.setFieldValue(mgr, BrasfootConstants.MANAGER_CONFIDENCE_FANS, 100);
                        System.out.println(ConsoleHelper.success("Confidence (Board/Fan) maximized to 100 for manager: "
                                + ReflectionUtils.getFieldValue(mgr, BrasfootConstants.MANAGER_NAME)));
                        found = true;
                        break;
                    }
                } catch (Exception ignored) {
                }
            }

            if (!found) {
                System.out.println(ConsoleHelper.warning("Human manager not found."));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to maximize confidence: " + e.getMessage());
        }
    }
    // --- Advanced Cheats ---

    public void expandStadium(Object team) {
        try {
            ReflectionUtils.setFieldValue(team, BrasfootConstants.TEAM_STADIUM_CAPACITY, 200000);
            System.out.println(ConsoleHelper.success("Stadium metrics boosted to 200,000!"));
        } catch (Exception e) {
            System.err.println(ConsoleHelper.error("Failed to expand stadium: " + e.getMessage()));
        }
    }

    public void amnesty(Object team) {
        try {
            List<?> players = (List<?>) ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_PLAYERS);
            if (players != null) {
                for (Object p : players) {

                    String[] cards = {
                            BrasfootConstants.CARD_YELLOW_1,
                            BrasfootConstants.CARD_YELLOW_2,
                            BrasfootConstants.CARD_RED,
                            "eD", "eE", "eF", "eG", "eR"
                    };
                    for (String c : cards) {
                        try {
                            ReflectionUtils.setFieldValue(p, c, 0);
                        } catch (Exception ignored) {
                        }
                    }
                }
                System.out.println(ConsoleHelper.success("Amnesty granted! Cards and suspensions cleared."));
            }
        } catch (Exception e) {
            System.err.println(ConsoleHelper.error("Failed amnesty: " + e.getMessage()));
        }
    }

    public void lifetimeContract(Object team) {
        try {
            List<?> players = (List<?>) ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_PLAYERS);
            if (players != null) {
                // 2050 Timestamp: approx 2524608000000L
                long future = 2524608000000L;
                for (Object p : players) {
                    try {
                        ReflectionUtils.setFieldValue(p, BrasfootConstants.PLAYER_CONTRACT_END, future);
                    } catch (Exception ignored) {
                    }
                }
                System.out.println(ConsoleHelper.success("Lifetime contracts signed (valid until 2050)!"));
            }
        } catch (Exception e) {
            System.err.println(ConsoleHelper.error("Failed contracts: " + e.getMessage()));
        }
    }

    public void stealPlayer(Object root, Object myTeam, String playerName) {
        try {
            System.out.println(ConsoleHelper.info("Scouting for: " + playerName));
            List<?> teams = (List<?>) ReflectionUtils.getFieldValue(root, BrasfootConstants.TEAMS_LIST);
            if (teams == null)
                return;

            Object targetPlayer = null;
            Object sourceTeam = null;

            // Find player
            for (Object t : teams) {
                if (t == myTeam)
                    continue;
                List<?> players = (List<?>) ReflectionUtils.getFieldValue(t, BrasfootConstants.TEAM_PLAYERS);
                if (players != null) {
                    for (Object p : players) {
                        String name = (String) ReflectionUtils.getFieldValue(p, BrasfootConstants.PLAYER_NAME);
                        if (StringUtils.normalize(name).equals(StringUtils.normalize(playerName))) {
                            targetPlayer = p;
                            sourceTeam = t;
                            break;
                        }
                    }
                }
                if (targetPlayer != null)
                    break;
            }

            if (targetPlayer != null && sourceTeam != null) {
                // Move player
                List<Object> sourceList = (List<Object>) ReflectionUtils.getFieldValue(sourceTeam,
                        BrasfootConstants.TEAM_PLAYERS);
                List<Object> destList = (List<Object>) ReflectionUtils.getFieldValue(myTeam,
                        BrasfootConstants.TEAM_PLAYERS);

                sourceList.remove(targetPlayer);
                destList.add(targetPlayer);

                // Update player's team ID reference if it exists ('bW' was 379 for team 379)
                try {
                    int myTeamId = (int) ReflectionUtils.getFieldValue(myTeam, BrasfootConstants.TEAM_ID);
                    ReflectionUtils.setFieldValue(targetPlayer, BrasfootConstants.PLAYER_ID, myTeamId);
                } catch (Exception ignored) {
                }

                System.out.println(ConsoleHelper.success("Signed " + playerName + " for FREE from "
                        + ReflectionUtils.getFieldValue(sourceTeam, BrasfootConstants.TEAM_NAME) + "!"));
            } else {
                System.out.println(ConsoleHelper.warning("Player not found."));
            }

        } catch (Exception e) {
            System.err.println(ConsoleHelper.error("Steal failed: " + e.getMessage()));
        }
    }

    // Clone logic requires deep copy or manual field copying.
    // Simplified clone: Create new instance of same class, copy visible fields.
    // However, recreating 'best.F' might be hard without constructor knowledge.
    // Reflection alloc?
    // Let's postpone Clone or try a naive clone.
    public void clonePlayer(Object myTeam, String playerName) {
        throw new UnsupportedOperationException("Not supported directly, use clonePlayerToMyTeam");
    }

    public void clonePlayerToMyTeam(Object root, Object myTeam, String playerName) {
        try {
            System.out.println(ConsoleHelper.info("Searching to clone: " + playerName));
            List<?> teams = (List<?>) ReflectionUtils.getFieldValue(root, BrasfootConstants.TEAMS_LIST);
            if (teams == null)
                return;

            Object targetPlayer = null;

            // Find player
            for (Object t : teams) {
                List<?> players = (List<?>) ReflectionUtils.getFieldValue(t, BrasfootConstants.TEAM_PLAYERS);
                if (players != null) {
                    for (Object p : players) {
                        String name = (String) ReflectionUtils.getFieldValue(p, BrasfootConstants.PLAYER_NAME);
                        if (StringUtils.normalize(name).equals(StringUtils.normalize(playerName))) {
                            targetPlayer = p;
                            break;
                        }
                    }
                }
                if (targetPlayer != null)
                    break;
            }

            if (targetPlayer != null) {
                // Clone!
                // Since we don't have a copy constructor, we create a new instance of the same
                // class
                Object clone = targetPlayer.getClass().getDeclaredConstructor().newInstance();

                // Copy all fields!
                for (Field f : targetPlayer.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(targetPlayer);
                    f.set(clone, val);
                }

                // Customize clone
                ReflectionUtils.setFieldValue(clone, BrasfootConstants.PLAYER_NAME,
                        ReflectionUtils.getFieldValue(targetPlayer, BrasfootConstants.PLAYER_NAME) + " (Clone)");

                // Set correct Team ID
                int myTeamId = (int) ReflectionUtils.getFieldValue(myTeam, BrasfootConstants.TEAM_ID);
                ReflectionUtils.setFieldValue(clone, BrasfootConstants.PLAYER_ID, myTeamId); // bW

                // Add to my team
                List<Object> destList = (List<Object>) ReflectionUtils.getFieldValue(myTeam,
                        BrasfootConstants.TEAM_PLAYERS);
                destList.add(clone);

                System.out.println(ConsoleHelper.success("Player cloned successfully into your team!"));
            } else {
                System.out.println(ConsoleHelper.warning("Player not found to clone."));
            }
        } catch (Exception e) {
            System.err.println(ConsoleHelper.error("Clone failed: " + e.getMessage()));
        }
    }

    // Better implementation requiring Root to find player globally

}
