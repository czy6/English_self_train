package com.english.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveQuestionDTO{
    private String subType;
    private String difficulty;
    private String aiJson;
}
