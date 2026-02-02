package br.com.saveeditor.brasfoot.model;

import java.util.HashMap;
import java.util.Map;

public class Preset {
    private String name;
    private String type; // "PLAYER" or "TEAM"
    private Map<String, Object> attributes = new HashMap<>(); // For the main object (Player or Team)
    private Map<String, Object> player_attributes = new HashMap<>(); // E.g. when applying to a Team, apply this to all
                                                                     // players

    public Map<String, Object> getPlayer_attributes() {
        return player_attributes;
    }

    public void setPlayer_attributes(Map<String, Object> player_attributes) {
        this.player_attributes = player_attributes;
    }

    public Preset() {
    }

    public Preset(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }
}
