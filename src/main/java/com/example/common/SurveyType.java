package com.example.common;

import java.util.stream.Stream;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor 
@Cacheable
@Getter
@Setter
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SurveyType extends BaseLookup {
 
    private Boolean onboardingEnabled;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private SurveyType parentSurvey;
    
    private Integer sampleDefaultPercentage;
    
    private Long dynaformLayoutId;
    
    public enum SURVEY_TYPE implements ILookupType<SURVEY_TYPE> {
      ESSN_SURVEY("ESSN_SURVEY"),NPTP_SURVEY("NPTP_SURVEY"), IDP_SURVEY("IDP_SURVEY"), AID_CARD_SURVEY("AID_CARD_SURVEY") , RECERTIFICATION_SURVEY("RECERTIFICATION_SURVEY");
     
      private String code;

      private SURVEY_TYPE(String code) {
          this.code = code;
      }

      @JsonCreator
      public static SURVEY_TYPE decode(final String code) {
          return Stream.of(SURVEY_TYPE.values()).filter(targetEnum -> targetEnum.code.equalsIgnoreCase(code)).findFirst()
                  .orElse(null);
      }

      @JsonValue
      public String getCode() { 	
          return code;
      }
 
  }
}
