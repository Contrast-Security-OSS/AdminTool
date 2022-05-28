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

import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.api.Api;
import com.contrastsecurity.admintool.api.ControlsApi;
import com.contrastsecurity.admintool.json.RuleSerializer;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.Rule;
import com.contrastsecurity.admintool.model.SecurityControl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ControlExportWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private Organization org;
    private String dirPath;

    Logger logger = LogManager.getLogger("admintool");

    public ControlExportWithProgress(Shell shell, PreferenceStore ps, Organization org, String dirPath) {
        this.shell = shell;
        this.ps = ps;
        this.org = org;
        this.dirPath = dirPath;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("セキュリティ制御のエクスポート...", 100);
        Thread.sleep(300);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeAdapter(Rule.class, new RuleSerializer()).setPrettyPrinting().create();
        try {
            monitor.subTask("セキュリティ制御の情報を取得...");
            Api api = new ControlsApi(this.shell, this.ps, org);
            SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 70);
            sub1Monitor.beginTask("", 1);
            List<SecurityControl> controls = (List<SecurityControl>) api.get();
            sub1Monitor.worked(1);
            sub1Monitor.done();
            if (controls.isEmpty()) {
                this.shell.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        MessageDialog.openInformation(shell, "セキュリティ制御の出力", "セキュリティ制御が登録されていません。");
                    }
                });
                monitor.setCanceled(true);
            }
            if (monitor.isCanceled()) {
                return;
            }
            Thread.sleep(1000);
            Writer writer = new FileWriter(dirPath + "\\" + this.org.getName() + ".json");
            monitor.subTask("セキュリティ制御の情報を出力...");
            SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 30);
            sub2Monitor.beginTask("", 1);
            gson.toJson(controls, writer);
            writer.close();
            sub2Monitor.worked(1);
            sub2Monitor.done();
            Thread.sleep(500);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        monitor.done();
        this.shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                MessageDialog.openInformation(shell, String.format("セキュリティ制御のエクスポート - %s", org.getName()),
                        String.format("JSONファイルを出力しました。\r\n%s", dirPath + "\\" + org.getName() + ".json"));
            }
        });
    }
}
