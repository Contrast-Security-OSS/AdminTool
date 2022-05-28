package com.contrastsecurity.admintool.json;

import java.lang.reflect.Type;

import com.contrastsecurity.admintool.model.ProtectionRule;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ProtectionRuleSerializer implements JsonSerializer<ProtectionRule> {

    @Override
    public JsonElement serialize(ProtectionRule rule, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(rule.getUuid());
    }

}
