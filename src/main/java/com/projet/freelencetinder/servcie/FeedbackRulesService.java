package com.projet.freelencetinder.servcie;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.projet.freelencetinder.models.CriterionType;
import com.projet.freelencetinder.models.FeedbackRole;

@Service
public class FeedbackRulesService {

    @Value("${feedback.minSampleForPublic:3}")
    private int minSampleForPublic;

    @Value("${feedback.windowDays.default:90}")
    private int windowDaysDefault;

    @Value("${feedback.windowDays.long:180}")
    private int windowDaysLong;

    public Map<CriterionType, BigDecimal> weightsFor(FeedbackRole role) {
        EnumMap<CriterionType, BigDecimal> map = new EnumMap<>(CriterionType.class);
        if (role == FeedbackRole.CLIENT_TO_FREELANCER) {
            map.put(CriterionType.QUALITY,            new BigDecimal("0.40"));
            map.put(CriterionType.TIMELINESS,         new BigDecimal("0.20"));
            map.put(CriterionType.COMMUNICATION,      new BigDecimal("0.20"));
            map.put(CriterionType.TECHNICAL,          new BigDecimal("0.10"));
            map.put(CriterionType.GLOBAL,             new BigDecimal("0.10"));
        } else {
            map.put(CriterionType.BRIEF_CLARITY,      new BigDecimal("0.30"));
            map.put(CriterionType.PAYMENT_RELIABILITY,new BigDecimal("0.30"));
            map.put(CriterionType.COMMUNICATION,      new BigDecimal("0.25"));
            map.put(CriterionType.ORGANIZATION,       new BigDecimal("0.10"));
            map.put(CriterionType.GLOBAL,             new BigDecimal("0.05"));
        }
        return map;
    }

    public int minSampleForPublic() { return minSampleForPublic; }
    public int windowDaysDefault() { return windowDaysDefault; }
    public int windowDaysLong() { return windowDaysLong; }
}


