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
import com.contrastsecurity.admintool.api.ControlCreateSanitizerApi;
import com.contrastsecurity.admintool.api.ControlCreateValidatorApi;
import com.contrastsecurity.admintool.exception.ApiException;
import com.contrastsecurity.admintool.exception.JsonException;
import com.contrastsecurity.admintool.json.RuleDeserializer;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.Rule;
import com.contrastsecurity.admintool.model.SecurityControl;
import com.contrastsecurity.admintool.preference.PreferenceConstants;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ControlImportWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private Organization org;
    private String filePath;
    private List<SecurityControl> successControls;
    private List<SecurityControl> failureControls;

    Logger logger = LogManager.getLogger("admintool");

    public ControlImportWithProgress(Shell shell, PreferenceStore ps, Organization org, String filePath) {
        this.shell = shell;
        this.ps = ps;
        this.org = org;
        this.filePath = filePath;
        this.successControls = new ArrayList<SecurityControl>();
        this.failureControls = new ArrayList<SecurityControl>();
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("セキュリティ制御のインポート...", 100);
        Thread.sleep(300);
        monitor.subTask("JSONファイルの読み込み...");
        SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 10);
        sub1Monitor.beginTask("", 1);
        List<SecurityControl> controls = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(filePath));
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Rule.class, new RuleDeserializer());
            controls = gsonBuilder.create().fromJson(reader, new TypeToken<List<SecurityControl>>() {
            }.getType());
            sub1Monitor.worked(1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        sub1Monitor.done();
        Thread.sleep(1000);

        monitor.subTask("セキュリティ制御の登録...");
        SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 90);
        sub2Monitor.beginTask("", controls.size());
        int sleep = this.ps.getInt(PreferenceConstants.SLEEP_SC_IMP);
        try {
            int cnt = 1;
            for (SecurityControl control : controls) {
                if (monitor.isCanceled()) {
                    throw new InterruptedException("キャンセルされました。");
                }
                monitor.subTask(String.format("セキュリティ制御をインポート...%s (%d/%d)", control.getName(), cnt++, controls.size()));
                String type = control.getType();
                Api api = null;
                try {
                    if (type.equals("SANITIZER")) {
                        api = new ControlCreateSanitizerApi(shell, this.ps, org, control);
                    } else if (type.equals("INPUT_VALIDATOR")) {
                        api = new ControlCreateValidatorApi(shell, this.ps, org, control);
                    } else {
                        control.setRemarks(String.format("セキュリティ制御のタイプが判別できません。%s", type));
                        this.failureControls.add(control);
                        sub2Monitor.worked(1);
                        continue;
                    }
                    String msg = (String) api.post();
                    if (Boolean.valueOf(msg)) {
                        this.successControls.add(control);
                    } else {
                        this.failureControls.add(control);
                    }
                } catch (JsonException je) {
                    control.setRemarks(je.getMessage());
                    this.failureControls.add(control);
                } catch (ApiException apie) {
                    control.setRemarks(apie.getMessage());
                    this.failureControls.add(control);
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
        ControlImportResultDialog dialog = new ControlImportResultDialog(shell, this.successControls, this.failureControls);
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
