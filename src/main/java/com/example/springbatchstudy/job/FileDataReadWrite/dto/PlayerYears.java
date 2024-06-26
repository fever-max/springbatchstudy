package com.example.springbatchstudy.job.FileDataReadWrite.dto;

import java.time.Year;

import lombok.Data;

@Data
public class PlayerYears {

    private String ID;
    private String lastName;
    private String firstName;
    private String position;
    private int birthYear;
    private int debutYear;
    private int yearsExperience;

    public PlayerYears(Player player) {
        this.ID = player.getID();
        this.lastName = player.getLastName();
        this.firstName = player.getFirstName();
        this.position = player.getPosition();
        this.birthYear = player.getBirthYear();
        this.debutYear = player.getDebutYear();
        // 현재년도-데뷔날짜 = 경력
        this.yearsExperience = Year.now().getValue() - player.getDebutYear();
    }

}
