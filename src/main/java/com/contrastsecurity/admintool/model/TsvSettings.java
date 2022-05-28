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

public class TsvSettings {

    private boolean tsv_enabled;
    private String tsv_type;
    private String tsv_device;
    private String google_configured;

    public boolean isTsv_enabled() {
        return tsv_enabled;
    }

    public void setTsv_enabled(boolean tsv_enabled) {
        this.tsv_enabled = tsv_enabled;
    }

    public String getTsv_type() {
        return tsv_type;
    }

    public void setTsv_type(String tsv_type) {
        this.tsv_type = tsv_type;
    }

    public String getTsv_device() {
        return tsv_device;
    }

    public void setTsv_device(String tsv_device) {
        this.tsv_device = tsv_device;
    }

    public String getGoogle_configured() {
        return google_configured;
    }

    public void setGoogle_configured(String google_configured) {
        this.google_configured = google_configured;
    }

}
