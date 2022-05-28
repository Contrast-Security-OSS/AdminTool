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
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.api.Api;
import com.contrastsecurity.admintool.api.ExclusionsApi;
import com.contrastsecurity.admintool.json.AssessmentRuleDeserializer;
import com.contrastsecurity.admintool.json.ProtectionRuleDeserializer;
import com.contrastsecurity.admintool.model.AssessmentRule;
import com.contrastsecurity.admintool.model.Exclusion;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.ProtectionRule;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ExclusionCompareWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private AppInfo appInfo;
    private String replaceBef;
    private String replaceAft;
    private String filePath;

    Logger logger = LogManager.getLogger("admintool");

    public ExclusionCompareWithProgress(Shell shell, PreferenceStore ps, AppInfo appInfo, String replaceBef, String replaceAft, String filePath) {
        this.shell = shell;
        this.ps = ps;
        this.appInfo = appInfo;
        this.replaceBef = replaceBef;
        this.replaceAft = replaceAft;
        this.filePath = filePath;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("例外のインポート済みチェック...", 100);
        Thread.sleep(300);
        monitor.subTask("JSONファイルの読み込み...");
        SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 30);
        sub1Monitor.beginTask("", 1);
        List<Exclusion> impExclusions = null;
        List<Exclusion> expExclusions = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(filePath));
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(AssessmentRule.class, new AssessmentRuleDeserializer());
            gsonBuilder.registerTypeAdapter(ProtectionRule.class, new ProtectionRuleDeserializer());
            impExclusions = gsonBuilder.create().fromJson(reader, new TypeToken<List<Exclusion>>() {
            }.getType());
            sub1Monitor.worked(1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        impExclusions.forEach(ex -> ex.setReplaceBef(this.replaceBef));
        impExclusions.forEach(ex -> ex.setReplaceAft(this.replaceAft));

        sub1Monitor.done();
        Thread.sleep(1000);

        monitor.subTask("例外の情報を取得...");
        SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 70);
        sub2Monitor.beginTask("", 1);
        Organization org = this.appInfo.getOrganization();
        String appName = this.appInfo.getAppName();
        String appId = this.appInfo.getAppId();
        try {
            Api api = new ExclusionsApi(this.shell, this.ps, org, appId);
            expExclusions = (List<Exclusion>) api.get();
            sub2Monitor.worked(1);
            Thread.sleep(500);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        sub2Monitor.done();

        monitor.done();

        List<String> impExclusionStrs = impExclusions.stream().map(ex -> ex.toString()).collect(Collectors.toList());
        List<String> expExclusionStrs = expExclusions.stream().map(ex -> ex.toString()).collect(Collectors.toList());
        List<String> problemStrs = new ArrayList<String>();
        for (String str : impExclusionStrs) {
            if (str.contains("IM-AccelPlatform-2022Spring-CRYPTOBADCIPHERS-System-00001")) {
                System.out.println(str);
            }
        }
        for (String str : expExclusionStrs) {
            if (str.contains("IM-AccelPlatform-2022Spring-CRYPTOBADCIPHERS-System-00001")) {
                System.out.println(str);
            }
        }
        for (String str : impExclusionStrs) {
            if (!expExclusionStrs.contains(str)) {
                problemStrs.add(str);
            }
        }

        if (problemStrs.isEmpty()) {
            this.shell.getDisplay().syncExec(new Runnable() {
                public void run() {
                    MessageDialog.openInformation(shell, "例外のインポート済み確認", String.format("JSONファイル内の例外はすべて%sに登録されています。", appInfo.getAppName()));
                }
            });
        } else {
            ExclusionCompareResultDialog dialog = new ExclusionCompareResultDialog(shell, problemStrs);
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
}
