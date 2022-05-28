package com.contrastsecurity.admintool.model;

import java.util.ArrayList;
import java.util.List;

public class AssessRulesConfiguration {
    private String rule_name;
    private String rule_title;
    private String rule_description;

    public String getRule_name() {
        return rule_name;
    }

    public void setRule_name(String rule_name) {
        this.rule_name = rule_name;
    }

    public String getRule_title() {
        return rule_title;
    }

    public void setRule_title(String rule_title) {
        this.rule_title = rule_title;
    }

    public String getRule_description() {
        return rule_description;
    }

    public void setRule_description(String rule_description) {
        this.rule_description = rule_description;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add("name: " + this.rule_name);
        strList.add("title: " + this.rule_title);
        strList.add("description: " + this.rule_description);
        return String.join("\r\n", strList);
    }
}
