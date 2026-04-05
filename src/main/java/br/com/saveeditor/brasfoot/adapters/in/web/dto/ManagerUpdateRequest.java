package br.com.saveeditor.brasfoot.adapters.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ManagerUpdateRequest {
    private String name;
    
    @Min(15)
    @Max(100)
    private Integer age;
    
    private String nationality;
    private Integer reputation;
    private Integer trophies;

    public ManagerUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public Integer getReputation() { return reputation; }
    public void setReputation(Integer reputation) { this.reputation = reputation; }

    public Integer getTrophies() { return trophies; }
    public void setTrophies(Integer trophies) { this.trophies = trophies; }
}
