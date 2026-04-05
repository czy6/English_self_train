package com.english.langchain4j.domain.po;

import lombok.Data;

@Data
public class Recommendation {

    private String mainType;
    private String subType;
    private String difficulty;
    private Double ProficiencyScore;

}
