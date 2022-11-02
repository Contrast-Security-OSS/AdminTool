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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Exclusion {

    public Exclusion() {
        this.assessment_rules = new ArrayList<AssessmentRule>();
        this.protection_rules = new ArrayList<ProtectionRule>();
        this.queues = new ArrayList<String>();
    }

    @Expose(serialize = true)
    private String name;

    @Expose(serialize = true)
    private String type;

    @Expose(serialize = true)
    private List<String> urls;

    @Expose(serialize = true)
    private List<String> codes;

    @Expose(serialize = true)
    private boolean assess;

    @Expose(serialize = false)
    private boolean defend;

    @Expose(serialize = false)
    private int exception_id;

    @Expose(serialize = true)
    private String input_type;

    @Expose(serialize = true)
    private String input_name;

    @Expose(serialize = true)
    private String url_pattern_type;

    @Expose(serialize = true)
    private boolean all_rules;

    @Expose(serialize = true)
    private boolean all_assessment_rules;

    @Expose(serialize = true)
    private boolean all_protection_rules;

    @Expose(serialize = true)
    @SerializedName(value = "assessment_rules", alternate = { "assess_rules" })
    private List<AssessmentRule> assessment_rules;

    @Expose(serialize = true)
    private List<ProtectionRule> protection_rules;

    @Expose(serialize = true)
    private String queue_pattern_type;

    @Expose(serialize = true)
    private List<String> queues;

    @Expose(serialize = false)
    private String replaceBef;
    @Expose(serialize = false)
    private String replaceAft;
    @Expose(serialize = false)
    private boolean deleteFlg;
    @Expose(serialize = false)
    private String remarks;

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

    public boolean isAll_rules() {
        return all_rules;
    }

    public void setAll_rules(boolean all_rules) {
        this.all_rules = all_rules;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    public boolean isAssess() {
        return assess;
    }

    public void setAssess(boolean assess) {
        this.assess = assess;
    }

    public boolean isDefend() {
        return defend;
    }

    public void setDefend(boolean defend) {
        this.defend = defend;
    }

    public int getException_id() {
        return exception_id;
    }

    public void setException_id(int exception_id) {
        this.exception_id = exception_id;
    }

    public String getInput_type() {
        return input_type;
    }

    public void setInput_type(String input_type) {
        this.input_type = input_type;
    }

    public String getInput_name() {
        return input_name;
    }

    public void setInput_name(String input_name) {
        this.input_name = input_name;
    }

    public String getUrl_pattern_type() {
        return url_pattern_type;
    }

    public void setUrl_pattern_type(String url_pattern_type) {
        this.url_pattern_type = url_pattern_type;
    }

    public List<AssessmentRule> getAssessment_rules() {
        return assessment_rules;
    }

    public void setAssessment_rules(List<AssessmentRule> assessment_rules) {
        this.assessment_rules = assessment_rules;
    }

    public List<ProtectionRule> getProtection_rules() {
        return protection_rules;
    }

    public void setProtection_rules(List<ProtectionRule> protection_rules) {
        this.protection_rules = protection_rules;
    }

    public boolean isAll_assessment_rules() {
        return all_assessment_rules;
    }

    public void setAll_assessment_rules(boolean all_assessment_rules) {
        this.all_assessment_rules = all_assessment_rules;
    }

    public boolean isAll_protection_rules() {
        return all_protection_rules;
    }

    public void setAll_protection_rules(boolean all_protection_rules) {
        this.all_protection_rules = all_protection_rules;
    }

    public String getQueue_pattern_type() {
        return queue_pattern_type;
    }

    public void setQueue_pattern_type(String queue_pattern_type) {
        this.queue_pattern_type = queue_pattern_type;
    }

    public List<String> getQueues() {
        return queues;
    }

    public void setQueues(List<String> queues) {
        this.queues = queues;
    }

    public String getReplaceBef() {
        return replaceBef;
    }

    public void setReplaceBef(String replaceBef) {
        this.replaceBef = replaceBef;
    }

    public String getReplaceAft() {
        return replaceAft;
    }

    public void setReplaceAft(String replaceAft) {
        this.replaceAft = replaceAft;
    }

    public boolean isDeleteFlg() {
        return deleteFlg;
    }

    public void setDeleteFlg(boolean deleteFlg) {
        this.deleteFlg = deleteFlg;
    }

    public String getRemarks() {
        if (remarks == null) {
            return "";
        }
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add("name: " + this.name);
        strList.add("type: " + this.type);
        strList.add("input_type: " + this.input_type);
        strList.add("input_name: " + this.input_name);
        strList.add("all_rules: " + this.all_rules);
        strList.add("all_assessment_rules: " + this.all_assessment_rules);
        strList.add("all_protection_rules   : " + this.all_protection_rules);
        strList.add("url_pattern_type: " + this.url_pattern_type);
        if (this.codes != null && !this.codes.isEmpty()) {
            Collections.sort(this.codes);
            strList.add("codes: " + String.join(", ", this.codes));
        }
        if (this.urls != null && !this.urls.isEmpty()) {
            Collections.sort(this.urls);
            if (this.replaceBef != null && this.replaceAft != null) {
                strList.add("urls: " + String.join(", ", this.urls.stream().map(s -> s.replaceAll(this.replaceBef, this.replaceAft)).collect(Collectors.toList())));
            } else {
                strList.add("urls: " + String.join(", ", this.urls));
            }
        }
        if (this.assessment_rules != null && !this.assessment_rules.isEmpty()) {
            Collections.sort(this.assessment_rules);
            strList.add("assessment_rules: " + String.join(", ", this.assessment_rules.stream().map(r -> r.getName()).collect(Collectors.toList())));
        }
        if (this.protection_rules != null && !this.protection_rules.isEmpty()) {
            Collections.sort(this.protection_rules);
            strList.add("protection_rules: " + String.join(", ", this.protection_rules.stream().map(r -> r.getUuid()).collect(Collectors.toList())));
        }
        strList.add("queue_pattern_type: " + (this.queue_pattern_type != null ? this.queue_pattern_type : "ALL"));
        if (this.queues != null && !this.queues.isEmpty()) {
            Collections.sort(this.queues);
            strList.add("queues: " + String.join(", ", this.queues));
        }
        return String.join(", ", strList);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Exclusion) {
            Exclusion other = (Exclusion) obj;
            return other.name.equals(this.name) && other.type.equals(this.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode()) + ((type == null) ? 0 : type.hashCode());
        return result;
    }
}
