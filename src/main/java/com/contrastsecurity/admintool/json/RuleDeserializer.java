package com.contrastsecurity.admintool.json;

import java.lang.reflect.Type;

import com.contrastsecurity.admintool.model.Rule;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class RuleDeserializer implements JsonDeserializer<Rule> {

    @Override
    public Rule deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Rule rule = new Rule();
        rule.setName(json.getAsString());
        return rule;
    }

}
