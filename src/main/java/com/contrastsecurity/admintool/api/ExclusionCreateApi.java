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

package com.contrastsecurity.admintool.api;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.exception.JsonException;
import com.contrastsecurity.admintool.json.ContrastJson;
import com.contrastsecurity.admintool.model.Exclusion;
import com.contrastsecurity.admintool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ExclusionCreateApi extends Api {

    private String appId;
    private Exclusion exclusion;
    public static final String[] INPUT_TYPE_FOR_WI_NAME = { "PARAMETER", "HEADER", "COOKIE" };
    public static final String[] INPUT_TYPE_FOR_WO_NAME = { "QUERYSTRING", "BODY" };

    public ExclusionCreateApi(Shell shell, IPreferenceStore ps, Organization org, String appId, Exclusion exclusion) {
        super(shell, ps, org);
        this.appId = appId;
        this.exclusion = exclusion;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        return String.format("%s/api/ng/%s/applications/%s/exclusions?expand=skip_links", this.contrastUrl, orgId, this.appId);
    }

    @Override
    protected RequestBody getBody() throws Exception {
        MediaType mediaTypeJson = MediaType.parse("application/json; charset=UTF-8");
        String name = this.exclusion.getName();
        String type = this.exclusion.getType();
        String input_name = this.exclusion.getInput_name();
        String input_type = this.exclusion.getInput_type();
        String url_pattern_type = this.exclusion.getUrl_pattern_type();
        boolean all_rules = this.exclusion.isAll_rules();
        boolean all_assessment_rules = this.exclusion.isAll_assessment_rules();
        boolean all_protection_rules = this.exclusion.isAll_protection_rules();

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("type", type);
        if (type.equals("CODE")) {
            map.put("urls", new ArrayList<String>());
            if (this.exclusion.getCodes() == null || this.exclusion.getCodes().isEmpty()) {
                throw new JsonException("codesの指定がありません。");
            }
            map.put("codes", this.exclusion.getCodes());
        } else if (type.equals("INPUT")) {
            map.put("codes", new ArrayList<String>());
            if (input_name == null || input_name.isEmpty()) {
                if (!Arrays.asList(INPUT_TYPE_FOR_WO_NAME).contains(input_type)) {
                    throw new JsonException("input_typeの指定が不正です。");
                }
                map.put("input_type", input_type);
            } else {
                map.put("input_name", input_name);
                if (!Arrays.asList(INPUT_TYPE_FOR_WI_NAME).contains(input_type)) {
                    throw new JsonException("input_typeの指定が不正です。");
                }
                map.put("input_type", input_type);
            }
            if (url_pattern_type.equals("ALL")) {
                map.put("url_pattern_type", "ALL");
                map.put("urls", new ArrayList<String>());
            } else {
                map.put("url_pattern_type", "ONLY");
                if (this.exclusion.getUrls() == null || this.exclusion.getUrls().isEmpty()) {
                    throw new JsonException("urlsの指定がありません。");
                }
                map.put("urls",
                        this.exclusion.getUrls().stream().map(s -> s.replaceAll(this.exclusion.getReplaceBef(), this.exclusion.getReplaceAft())).collect(Collectors.toList()));
            }
        } else if (type.equals("URL")) {
            map.put("codes", new ArrayList<String>());
            if (url_pattern_type.equals("ALL")) {
                map.put("url_pattern_type", "ALL");
                map.put("urls", new ArrayList<String>());
            } else {
                map.put("url_pattern_type", "ONLY");
                if (this.exclusion.getUrls() == null || this.exclusion.getUrls().isEmpty()) {
                    throw new JsonException("urlsの指定がありません。");
                }
                map.put("urls",
                        this.exclusion.getUrls().stream().map(s -> s.replaceAll(this.exclusion.getReplaceBef(), this.exclusion.getReplaceAft())).collect(Collectors.toList()));
            }
        } else {
            throw new JsonException("typeの指定が不正です。");
        }

        if (all_rules) {
            map.put("all_rules", true);
            map.put("all_assessment_rules", false);
            map.put("assessment_rules", new ArrayList<String>());
            map.put("all_protection_rules", false);
            map.put("protection_rules", new ArrayList<String>());
        } else {
            if (all_assessment_rules) {
                map.put("all_rules", false);
                map.put("all_assessment_rules", true);
                map.put("assessment_rules", new ArrayList<String>());
                map.put("all_protection_rules", false);
                map.put("protection_rules", new ArrayList<String>());
            } else if (all_protection_rules) {
                map.put("all_rules", false);
                map.put("all_assessment_rules", false);
                map.put("assessment_rules", new ArrayList<String>());
                map.put("all_protection_rules", true);
                map.put("protection_rules", new ArrayList<String>());
            } else {
                map.put("all_rules", false);
                map.put("all_assessment_rules", false);
                map.put("all_protection_rules", false);
                if (this.exclusion.getAssessment_rules() == null && this.exclusion.getProtection_rules() == null) {
                    throw new JsonException("assessment_rulesまたはprotection_rulesの指定がありません。");
                }
                List<String> assessmentRules = this.exclusion.getAssessment_rules().stream().map(rule -> rule.getName()).collect(Collectors.toList());
                List<String> protectionRules = this.exclusion.getProtection_rules().stream().map(rule -> rule.getName()).collect(Collectors.toList());
                if (assessmentRules.isEmpty() && protectionRules.isEmpty()) {
                    throw new JsonException("assessment_rulesまたはprotection_rulesの指定がありません。");
                }
                map.put("assessment_rules", assessmentRules);
                map.put("protection_rules", protectionRules);
            }
        }
        Gson gson = new Gson();
        String json = gson.toJson(map);
        return RequestBody.create(json, mediaTypeJson);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type contType = new TypeToken<ContrastJson>() {
        }.getType();
        ContrastJson contrastJson = gson.fromJson(response, contType);
        return contrastJson.getSuccess();
    }

}
