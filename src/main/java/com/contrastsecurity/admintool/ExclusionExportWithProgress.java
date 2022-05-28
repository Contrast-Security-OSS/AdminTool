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
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.api.Api;
import com.contrastsecurity.admintool.api.ExclusionsApi;
import com.contrastsecurity.admintool.json.RuleSerializer;
import com.contrastsecurity.admintool.model.Exclusion;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.Rule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExclusionExportWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private List<String> dstApps;
    private Map<String, AppInfo> fullAppMap;
    private String dirPath;

    Logger logger = LogManager.getLogger("admintool");

    public ExclusionExportWithProgress(Shell shell, PreferenceStore ps, List<String> dstApps, Map<String, AppInfo> fullAppMap, String dirPath) {
        this.shell = shell;
        this.ps = ps;
        this.dstApps = dstApps;
        this.fullAppMap = fullAppMap;
        this.dirPath = dirPath;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("例外のエクスポート...", 100 * dstApps.size());
        Thread.sleep(300);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeAdapter(Rule.class, new RuleSerializer()).setPrettyPrinting().create();
        Organization org = fullAppMap.values().iterator().next().getOrganization();
        try {
            int appIdx = 1;
            for (String appLabel : dstApps) {
                SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 70);
                sub1Monitor.beginTask("", 1);
                String appName = fullAppMap.get(appLabel).getAppName();
                String appId = fullAppMap.get(appLabel).getAppId();
                monitor.setTaskName(String.format("[%s] %s (%d/%d)", org.getName(), appName, appIdx, dstApps.size()));
                monitor.subTask("例外の情報を取得...");
                Api api = new ExclusionsApi(this.shell, this.ps, org, appId);
                List<Exclusion> exclusions = (List<Exclusion>) api.get();
                sub1Monitor.worked(1);
                sub1Monitor.done();

                monitor.subTask("例外の情報を出力...");
                SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 30);
                sub2Monitor.beginTask("", 1);
                if (exclusions.isEmpty()) {
                    sub2Monitor.worked(1);
                    sub2Monitor.done();
                    continue;
                }
                Thread.sleep(1000);
                Writer writer = new FileWriter(dirPath + "\\" + org.getName() + "_" + appName + ".json");
                gson.toJson(exclusions, writer);
                writer.close();
                sub2Monitor.worked(1);
                sub2Monitor.done();
                Thread.sleep(500);
                appIdx++;
            }
            monitor.subTask("");
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        monitor.done();
        this.shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                MessageDialog.openInformation(shell, String.format("例外のエクスポート - %s", org.getName()), String.format("JSONファイルを出力しました。\r\n%s", dirPath));
            }
        });
    }
}
