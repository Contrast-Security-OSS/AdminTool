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
import com.contrastsecurity.admintool.api.ControlsApi;
import com.contrastsecurity.admintool.json.RuleDeserializer;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.Rule;
import com.contrastsecurity.admintool.model.SecurityControl;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ControlCompareWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private Organization org;
    private String filePath;

    Logger logger = LogManager.getLogger("admintool");

    public ControlCompareWithProgress(Shell shell, PreferenceStore ps, Organization org, String filePath) {
        this.shell = shell;
        this.ps = ps;
        this.org = org;
        this.filePath = filePath;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("セキュリティ制御のインポート済みチェック...", 100);
        Thread.sleep(300);
        monitor.subTask("JSONファイルの読み込み...");
        SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 30);
        sub1Monitor.beginTask("", 1);
        List<SecurityControl> impControls = null;
        List<SecurityControl> expControls = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(filePath));
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Rule.class, new RuleDeserializer());
            impControls = gsonBuilder.create().fromJson(reader, new TypeToken<List<SecurityControl>>() {
            }.getType());
            sub1Monitor.worked(1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        sub1Monitor.done();
        Thread.sleep(1000);

        monitor.subTask("セキュリティ制御の情報を取得...");
        SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 70);
        sub2Monitor.beginTask("", 1);
        try {
            Api securityControlsApi = new ControlsApi(this.shell, this.ps, org);
            expControls = (List<SecurityControl>) securityControlsApi.get();
            sub2Monitor.worked(1);
            Thread.sleep(500);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        sub2Monitor.done();

        monitor.done();

        List<String> impControlStrs = impControls.stream().map(sc -> sc.toString()).collect(Collectors.toList());
        List<String> expControlStrs = expControls.stream().map(sc -> sc.toString()).collect(Collectors.toList());
        List<String> problemStrs = new ArrayList<String>();
        List<SecurityControl> problemControls = new ArrayList<SecurityControl>();
        for (int i = 0; i < impControlStrs.size(); i++) {
            String str = impControlStrs.get(i);
            if (!expControlStrs.contains(str)) {
                problemStrs.add(str);
                problemControls.add(impControls.get(i));
            }
        }

        if (problemStrs.isEmpty()) {
            this.shell.getDisplay().syncExec(new Runnable() {
                public void run() {
                    MessageDialog.openInformation(shell, "セキュリティ制御のインポート済み確認", String.format("JSONファイル内のセキュリティ制御はすべて%sに登録されています。", org.getName()));
                }
            });
        } else {
            ControlCompareResultDialog dialog = new ControlCompareResultDialog(shell, org, problemStrs, problemControls);
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
