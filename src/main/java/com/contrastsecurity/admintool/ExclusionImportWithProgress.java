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

package com.contrastsecurity.admintool;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.api.Api;
import com.contrastsecurity.admintool.api.ExclusionCreateApi;
import com.contrastsecurity.admintool.exception.ApiException;
import com.contrastsecurity.admintool.exception.JsonException;
import com.contrastsecurity.admintool.json.AssessmentRuleDeserializer;
import com.contrastsecurity.admintool.json.ContrastJson;
import com.contrastsecurity.admintool.json.ProtectionRuleDeserializer;
import com.contrastsecurity.admintool.model.AssessmentRule;
import com.contrastsecurity.admintool.model.Exclusion;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.ProtectionRule;
import com.contrastsecurity.admintool.preference.PreferenceConstants;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ExclusionImportWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private AppInfo appInfo;;
    private String replaceBef;
    private String replaceAft;
    private String filePath;
    private List<Exclusion> successControls;
    private List<Exclusion> failureControls;

    Logger logger = LogManager.getLogger("admintool");

    public ExclusionImportWithProgress(Shell shell, PreferenceStore ps, AppInfo appInfo, String replaceBef, String replaceAft, String filePath) {
        this.shell = shell;
        this.ps = ps;
        this.appInfo = appInfo;
        this.replaceBef = replaceBef;
        this.replaceAft = replaceAft;
        this.filePath = filePath;
        this.successControls = new ArrayList<Exclusion>();
        this.failureControls = new ArrayList<Exclusion>();
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("例外のインポート...", 100);
        Thread.sleep(300);
        monitor.subTask("JSONファイルの読み込み...");
        SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 10);
        sub1Monitor.beginTask("", 1);
        List<Exclusion> exclusions = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(filePath));
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(AssessmentRule.class, new AssessmentRuleDeserializer());
            gsonBuilder.registerTypeAdapter(ProtectionRule.class, new ProtectionRuleDeserializer());
            exclusions = gsonBuilder.create().fromJson(reader, new TypeToken<List<Exclusion>>() {
            }.getType());
            sub1Monitor.worked(1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        sub1Monitor.done();
        Thread.sleep(1000);

        monitor.subTask("例外の登録...");
        SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 90);
        sub2Monitor.beginTask("", exclusions.size());
        Organization org = this.appInfo.getOrganization();
        String appName = this.appInfo.getAppName();
        String appId = this.appInfo.getAppId();
        int sleep = this.ps.getInt(PreferenceConstants.SLEEP_EX_IMP);
        try {
            int cnt = 1;
            for (Exclusion exclusion : exclusions) {
                if (monitor.isCanceled()) {
                    throw new InterruptedException("キャンセルされました。");
                }
                monitor.subTask(String.format("例外をインポート...%s (%d/%d)", exclusion.getName(), cnt++, exclusions.size()));
                String type = exclusion.getType();
                exclusion.setReplaceBef(this.replaceBef);
                exclusion.setReplaceAft(this.replaceAft);
                Api api = null;
                try {
                    api = new ExclusionCreateApi(shell, this.ps, org, appId, exclusion);
                    ContrastJson json = (ContrastJson) api.post();
                    if (Boolean.valueOf(json.getSuccess())) {
                        this.successControls.add(exclusion);
                    } else {
                        exclusion.setRemarks(json.getErrors().toString());
                        this.failureControls.add(exclusion);
                    }
                } catch (JsonException je) {
                    exclusion.setRemarks(je.getMessage());
                    this.failureControls.add(exclusion);
                } catch (ApiException apie) {
                    exclusion.setRemarks(apie.getMessage());
                    this.failureControls.add(exclusion);
                }
                sub2Monitor.worked(1);
                Thread.sleep(sleep);
            }
            Thread.sleep(500);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        sub2Monitor.done();

        monitor.done();
        ExclusionImportResultDialog dialog = new ExclusionImportResultDialog(shell, appInfo, this.successControls, this.failureControls);
        this.shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                int result = dialog.open();
                if (IDialogConstants.OK_ID != result) {
                    monitor.setCanceled(true);
                }
            }
        });

    }
}
