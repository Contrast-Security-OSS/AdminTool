package com.contrastsecurity.admintool.model;

import java.util.ArrayList;
import java.util.List;

public class ProtectionPolicy implements Comparable<ProtectionPolicy> {
    private String name;
    private String type;
    private String uuid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add("name: " + this.name);
        strList.add("type: " + this.type);
        strList.add("uuid: " + this.uuid);
        return String.join("\r\n", strList);
    }

    @Override
    public int compareTo(ProtectionPolicy other) {
        return uuid.compareTo(other.getUuid());
    }

}
