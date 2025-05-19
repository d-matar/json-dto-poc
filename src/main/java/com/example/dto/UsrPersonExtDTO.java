package com.example.dto;

import java.io.Serializable;

import com.example.common.SurveyType;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UsrPersonExtDTO implements Serializable {
    private static final long serialVersionUID = 6732498561023479582L;

    @Builder.Default
    private final SurveyType.SURVEY_TYPE surveyType = SurveyType.SURVEY_TYPE.RECERTIFICATION_SURVEY;

    private JsonNode extendedAttributes;
}
