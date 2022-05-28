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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.json.TsvSettingsJson;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.TsvSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TsvSettingsApi extends Api {

    public TsvSettingsApi(Shell shell, IPreferenceStore ps, Organization org, String contrastUrl, String userName, String seviceKey) {
        super(shell, ps, org);
        this.contrastUrl = contrastUrl;
        this.userName = userName;
        this.serviceKey = seviceKey;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        return String.format("%s/api/ng/%s/tsv/user?expand=skip_links", this.contrastUrl, orgId);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type contType = new TypeToken<TsvSettingsJson>() {
        }.getType();
        TsvSettingsJson tsvSettingsJson = gson.fromJson(response, contType);
        TsvSettings tsvSettings = tsvSettingsJson.getTsv();
        return tsvSettings;
    }

}
