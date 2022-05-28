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

public class UserInput {
    private String value;
    private String name;
    private String type;
    private boolean omitted_value;
    private boolean truncated_value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

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

    public boolean isOmitted_value() {
        return omitted_value;
    }

    public void setOmitted_value(boolean omitted_value) {
        this.omitted_value = omitted_value;
    }

    public boolean isTruncated_value() {
        return truncated_value;
    }

    public void setTruncated_value(boolean truncated_value) {
        this.truncated_value = truncated_value;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add("value: " + this.value);
        strList.add("name: " + this.name);
        strList.add("type: " + this.type);
        strList.add("omitted_value: " + this.omitted_value);
        strList.add("truncated_value: " + this.truncated_value);
        return String.join(", ", strList);
    }

}
