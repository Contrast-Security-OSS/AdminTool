/*
 * MIT License
 * Copyright (c) 2020 Contrast Security Japan G.K.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package com.contrastsecurity.admintool.model;

import java.util.ArrayList;
import java.util.List;

public class ProtectionRule implements Comparable<ProtectionRule> {
    private String uuid;
    private String name;
    private String displayName;
    private String category;
    private String description;
    private String impact;
    private int id;
    private boolean cve_shield;
    private boolean can_block_at_perimeter;
    private boolean is_monitor_at_perimeter;
    private boolean can_block;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isCve_shield() {
        return cve_shield;
    }

    public void setCve_shield(boolean cve_shield) {
        this.cve_shield = cve_shield;
    }

    public boolean isCan_block_at_perimeter() {
        return can_block_at_perimeter;
    }

    public void setCan_block_at_perimeter(boolean can_block_at_perimeter) {
        this.can_block_at_perimeter = can_block_at_perimeter;
    }

    public boolean isIs_monitor_at_perimeter() {
        return is_monitor_at_perimeter;
    }

    public void setIs_monitor_at_perimeter(boolean is_monitor_at_perimeter) {
        this.is_monitor_at_perimeter = is_monitor_at_perimeter;
    }

    public boolean isCan_block() {
        return can_block;
    }

    public void setCan_block(boolean can_block) {
        this.can_block = can_block;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add("uuid: " + this.uuid);
        strList.add("name: " + this.name);
        strList.add("displayName: " + this.displayName);
        strList.add("category: " + this.category);
        strList.add("category: " + this.category);
        return String.join("\r\n", strList);
    }

    @Override
    public int compareTo(ProtectionRule other) {
        return uuid.compareTo(other.getUuid());
    }

}
