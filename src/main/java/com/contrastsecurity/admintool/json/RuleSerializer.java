package com.contrastsecurity.admintool.json;

import java.lang.reflect.Type;

import com.contrastsecurity.admintool.model.Rule;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RuleSerializer implements JsonSerializer<Rule> {

    @Override
    public JsonElement serialize(Rule rule, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(rule.getName());
    }

}
