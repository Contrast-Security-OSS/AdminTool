package com.contrastsecurity.admintool.json;

import java.lang.reflect.Type;

import com.contrastsecurity.admintool.model.AssessmentRule;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class AssessmentRuleDeserializer implements JsonDeserializer<AssessmentRule> {

    @Override
    public AssessmentRule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        AssessmentRule rule = new AssessmentRule();
        rule.setName(json.getAsString());
        return rule;
    }

}
