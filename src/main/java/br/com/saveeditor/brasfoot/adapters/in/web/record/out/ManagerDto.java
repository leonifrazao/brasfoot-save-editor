package br.com.saveeditor.brasfoot.adapters.in.web.record.out;

public class ManagerDto {
    private Integer id;
    private String name;
    private Boolean isHuman;
    private Integer teamId;
    private Integer confidenceBoard;
    private Integer confidenceFans;
    private Integer age;
    private String nationality;
    private Integer reputation;
    private Integer trophies;

    public ManagerDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsHuman() { return isHuman; }
    public void setIsHuman(Boolean isHuman) { this.isHuman = isHuman; }

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }

    public Integer getConfidenceBoard() { return confidenceBoard; }
    public void setConfidenceBoard(Integer confidenceBoard) { this.confidenceBoard = confidenceBoard; }

    public Integer getConfidenceFans() { return confidenceFans; }
    public void setConfidenceFans(Integer confidenceFans) { this.confidenceFans = confidenceFans; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public Integer getReputation() { return reputation; }
    public void setReputation(Integer reputation) { this.reputation = reputation; }

    public Integer getTrophies() { return trophies; }
    public void setTrophies(Integer trophies) { this.trophies = trophies; }
}
