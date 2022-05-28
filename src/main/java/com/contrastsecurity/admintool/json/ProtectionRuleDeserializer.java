package com.contrastsecurity.admintool.json;

import java.lang.reflect.Type;

import com.contrastsecurity.admintool.model.ProtectionRule;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class ProtectionRuleDeserializer implements JsonDeserializer<ProtectionRule> {

    @Override
    public ProtectionRule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ProtectionRule rule = new ProtectionRule();
        rule.setUuid(json.getAsString());
        return rule;
    }

}
