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

import java.util.Map;

public class ContrastSecurityYaml {

    private Map<String, Object> api;

    public Map<String, Object> getApi() {
        return api;
    }

    public void setApi(Map<String, Object> api) {
        this.api = api;
    }

    public String getUrl() {
        if (this.api == null) {
            return "";
        }
        if (this.api.containsKey("url")) {
            return this.api.get("url").toString();
        }
        return "";
    }

    public String getApiKey() {
        if (this.api == null) {
            return "";
        }
        if (this.api.containsKey("api_key")) {
            return this.api.get("api_key").toString();
        }
        return "";
    }

    public String getServiceKey() {
        if (this.api == null) {
            return "";
        }
        if (this.api.containsKey("service_key")) {
            return this.api.get("service_key").toString();
        }
        return "";
    }

    public String getUserName() {
        if (this.api == null) {
            return "";
        }
        if (this.api.containsKey("user_name")) {
            return this.api.get("user_name").toString();
        }
        return "";
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("url         : %s\n", getUrl()));
        builder.append(String.format("api_key     : %s\n", getApiKey()));
        builder.append(String.format("service_key : %s\n", getServiceKey()));
        builder.append(String.format("user_name   : %s\n", getUserName()));
        return builder.toString();
    }

}
