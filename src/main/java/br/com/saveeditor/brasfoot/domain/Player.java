package br.com.saveeditor.brasfoot.domain;

import lombok.*;

import br.com.saveeditor.brasfoot.domain.enums.PlayerCharacteristic;
import br.com.saveeditor.brasfoot.domain.enums.PlayerPosition;
import br.com.saveeditor.brasfoot.domain.enums.PlayerSide;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Builder
public class Player {

    @Setter
    private int id;

    @Setter
    private String name;
    private int age;
    private int overall;
    private int position;
    private int energy;
    private int morale;
    private Integer salary;
    private Integer side;
    private Long contractEnd;
    private Integer country;
    private Integer characteristic1;
    private Integer characteristic2;
    private Integer skillGoalkeeping;
    private Integer skillSpeed;
    private Integer skillTechnique;
    private Integer skillPassing;
    private Integer skillTackling;
    private Integer skillPlaymaking;
    private Integer skillFinishing;
    private boolean starLocal;
    private boolean starGlobal;

    public Player(int id, String name, int age, int overall, int position, int energy, int morale) {
        this(id, name, age, overall, position, energy, morale, false, false);
    }

    public Player(int id, String name, int age, int overall, int position, int energy, int morale, boolean starLocal, boolean starGlobal) {
        if (age < 15 || age > 50) {
            throw new IllegalArgumentException("Invalid age: must be between 15 and 50");
        }
        if (overall < 1 || overall > 100) {
            throw new IllegalArgumentException("Invalid overall: must be between 1 and 100");
        }
        if (PlayerPosition.fromCode(position).getCode() != position) {
            throw new IllegalArgumentException("Invalid position: must be 0 to 4");
        }
        if (energy < -1 || energy > 100) {
            throw new IllegalArgumentException("Invalid energy: must be between -1 and 100");
        }
        if (morale < 0 || morale > 100) {
            throw new IllegalArgumentException("Invalid morale: must be between 0 and 100");
        }
        this.id = id;
        this.name = name;
        this.age = age;
        this.overall = overall;
        this.position = position;
        this.energy = energy;
        this.morale = morale;
        this.starLocal = starLocal;
        this.starGlobal = starGlobal;
    }

    public Player(int id, String name, int age, int overall, int position, int energy, int morale, Integer salary,
                  Integer side, Long contractEnd, Integer characteristic1, Integer characteristic2,
                  Integer skillGoalkeeping, Integer skillSpeed, Integer skillTechnique, Integer skillPassing,
                  Integer skillTackling, Integer skillPlaymaking, Integer skillFinishing, Integer country,
                  boolean starLocal, boolean starGlobal) {
        this(id, name, age, overall, position, energy, morale, starLocal, starGlobal);
        setSalary(salary);
        setSide(side);
        setContractEnd(contractEnd);
        setCharacteristic1(characteristic1);
        setCharacteristic2(characteristic2);
        setSkillGoalkeeping(skillGoalkeeping);
        setSkillSpeed(skillSpeed);
        setSkillTechnique(skillTechnique);
        setSkillPassing(skillPassing);
        setSkillTackling(skillTackling);
        setSkillPlaymaking(skillPlaymaking);
        setSkillFinishing(skillFinishing);
        setCountry(country);
    }


    public void setAge(int value) {
        if (value < 15 || value > 50) {
            throw new IllegalArgumentException("Invalid age: must be between 15 and 50");
        }
        this.age = value;
    }


    public void setOverall(int value) {
        if (value < 1 || value > 100) {
            throw new IllegalArgumentException("Invalid overall: must be between 1 and 100");
        }
        this.overall = value;
    }


    public void setPosition(int value) {
        if (value < 0 || value > 4) {
            throw new IllegalArgumentException("Invalid position: must be 0 to 4");
        }
        this.position = value;
    }


    public void setEnergy(int value) {
        if (value < -1 || value > 100) {
            throw new IllegalArgumentException("Invalid energy: must be between -1 and 100");
        }
        this.energy = value;
    }


    public void setMorale(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Invalid morale: must be between 0 and 100");
        }
        this.morale = value;
    }

    public void setSalary(Integer salary) {
        if (salary != null && salary < 0) {
            throw new IllegalArgumentException("Invalid salary: cannot be negative");
        }
        this.salary = salary;
    }

    public void setSide(Integer side) {
        if (side != null && PlayerSide.fromCode(side).getCode() != side) {
            throw new IllegalArgumentException("Invalid side: must be 0 or 1");
        }
        this.side = side;
    }

    public void setCountry(Integer country) {
        this.country = country;
    }

    public void setContractEnd(Long contractEnd) {
        this.contractEnd = contractEnd;
    }

    public void setCharacteristic1(Integer characteristic1) {
        validateCharacteristic(characteristic1);
        this.characteristic1 = characteristic1;
    }

    public void setCharacteristic2(Integer characteristic2) {
        validateCharacteristic(characteristic2);
        this.characteristic2 = characteristic2;
    }

    public void setSkillGoalkeeping(Integer skillGoalkeeping) {
        validateSkill(skillGoalkeeping);
        this.skillGoalkeeping = skillGoalkeeping;
    }

    public void setSkillSpeed(Integer skillSpeed) {
        validateSkill(skillSpeed);
        this.skillSpeed = skillSpeed;
    }

    public void setSkillTechnique(Integer skillTechnique) {
        validateSkill(skillTechnique);
        this.skillTechnique = skillTechnique;
    }

    public void setSkillPassing(Integer skillPassing) {
        validateSkill(skillPassing);
        this.skillPassing = skillPassing;
    }

    public void setSkillTackling(Integer skillTackling) {
        validateSkill(skillTackling);
        this.skillTackling = skillTackling;
    }

    public void setSkillPlaymaking(Integer skillPlaymaking) {
        validateSkill(skillPlaymaking);
        this.skillPlaymaking = skillPlaymaking;
    }

    public void setSkillFinishing(Integer skillFinishing) {
        validateSkill(skillFinishing);
        this.skillFinishing = skillFinishing;
    }

    public void setStarLocal(boolean starLocal) {
        this.starLocal = starLocal;
    }

    public void setStarGlobal(boolean starGlobal) {
        this.starGlobal = starGlobal;
    }

    private void validateCharacteristic(Integer value) {
        if (value == null) {
            return;
        }
        if (PlayerCharacteristic.fromCode(value).getCode() != value) {
            throw new IllegalArgumentException("Invalid characteristic: must be between 0 and 13");
        }
    }

    private void validateSkill(Integer value) {
        if (value != null && (value < 0 || value > 100)) {
            throw new IllegalArgumentException("Invalid skill: must be between 0 and 100");
        }
    }

}
