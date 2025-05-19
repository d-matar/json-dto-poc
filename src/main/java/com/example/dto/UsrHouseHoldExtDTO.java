package com.example.dto;

import java.io.Serializable;

import com.example.common.SurveyType;
import com.fasterxml.jackson.databind.JsonNode;
//import com.sirenanalytics.impact_aid_card_common.model.entity.survey.IExtHouseHoldDataObject;
//import com.sirenanalytics.impact_aid_card_common.model.entity.survey.SurveyType;

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
public class UsrHouseHoldExtDTO implements Serializable {
    private static final long serialVersionUID = 8923641502389471234L;

    @Builder.Default
    private final SurveyType.SURVEY_TYPE surveyType = SurveyType.SURVEY_TYPE.RECERTIFICATION_SURVEY;

    private JsonNode extendedAttributes;
}
