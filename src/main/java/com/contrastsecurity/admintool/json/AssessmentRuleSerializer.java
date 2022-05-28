package com.contrastsecurity.admintool.json;

import java.lang.reflect.Type;

import com.contrastsecurity.admintool.model.AssessmentRule;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AssessmentRuleSerializer implements JsonSerializer<AssessmentRule> {

    @Override
    public JsonElement serialize(AssessmentRule rule, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(rule.getName());
    }

}
