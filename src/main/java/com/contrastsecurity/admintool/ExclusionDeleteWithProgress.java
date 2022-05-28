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

import java.lang.reflect.InvocationTargetException;
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
import com.contrastsecurity.admintool.api.ExclusionDeleteApi;
import com.contrastsecurity.admintool.api.ExclusionsApi;
import com.contrastsecurity.admintool.model.Exclusion;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.preference.PreferenceConstants;

public class ExclusionDeleteWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private AppInfo appInfo;;
    private String filterWord;
    private List<Exclusion> targetExclusions;

    Logger logger = LogManager.getLogger("admintool");

    public ExclusionDeleteWithProgress(Shell shell, PreferenceStore ps, AppInfo appInfo, String filterWord) {
        this.shell = shell;
        this.ps = ps;
        this.appInfo = appInfo;
        this.filterWord = filterWord;
        this.targetExclusions = new ArrayList<Exclusion>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("例外の削除...", 100);
        Thread.sleep(300);
        int sleep = this.ps.getInt(PreferenceConstants.SLEEP_EX_DEL);
        try {
            monitor.subTask("例外の情報を取得...");
            SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 100);
            Organization org = this.appInfo.getOrganization();
            String appName = this.appInfo.getAppName();
            String appId = this.appInfo.getAppId();
            Api exclusionsApi = new ExclusionsApi(this.shell, this.ps, org, appId);
            List<Exclusion> exclusions = (List<Exclusion>) exclusionsApi.get();
            sub1Monitor.beginTask("", exclusions.size());
            for (Exclusion exclusion : exclusions) {
                if (monitor.isCanceled()) {
                    throw new InterruptedException("キャンセルされました。");
                }
                monitor.subTask(String.format("例外の情報を取得...%s", exclusion.getName()));
                exclusion.setDeleteFlg(false);
                if (!filterWord.isEmpty()) {
                    if (filterWord.contains("*")) {
                        String word = filterWord.replace("*", "");
                        if (filterWord.startsWith("*") && filterWord.endsWith("*")) {
                            if (exclusion.getName().contains(word)) {
                                exclusion.setDeleteFlg(true);
                            }
                        } else if (filterWord.endsWith("*")) {
                            if (exclusion.getName().startsWith(word)) {
                                exclusion.setDeleteFlg(true);
                            }
                        } else if (filterWord.startsWith("*")) {
                            if (exclusion.getName().endsWith(word)) {
                                exclusion.setDeleteFlg(true);
                            }
                        }
                    } else {
                        if (exclusion.getName().equals(filterWord)) {
                            exclusion.setDeleteFlg(true);
                        }
                    }
                } else {
                    exclusion.setDeleteFlg(true);
                }
                this.targetExclusions.add(exclusion);
                sub1Monitor.worked(1);
                Thread.sleep(10);
            }
            sub1Monitor.done();

            ExclusionDeleteConfirmDialog dialog = new ExclusionDeleteConfirmDialog(shell, appInfo, this.targetExclusions);
            this.shell.getDisplay().syncExec(new Runnable() {
                public void run() {
                    int result = dialog.open();
                    if (IDialogConstants.OK_ID != result) {
                        monitor.setCanceled(true);
                    }
                }
            });
            if (monitor.isCanceled()) {
                return;
            }
            List<Integer> selectedIdxes = dialog.getSelectedIdxes();
            if (selectedIdxes.isEmpty()) {
                monitor.setCanceled(true);
            }
            if (monitor.isCanceled()) {
                return;
            }
            monitor.beginTask("例外の削除...", 100);
            SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 100);
            sub2Monitor.beginTask("", selectedIdxes.size());
            int cnt = 1;
            for (Integer index : selectedIdxes) {
                if (monitor.isCanceled()) {
                    return;
                }
                Exclusion control = this.targetExclusions.get(index);
                monitor.subTask(String.format("例外を削除...%s (%d/%d)", control.getName(), cnt++, selectedIdxes.size()));
                Api exclusionDeleteApi = new ExclusionDeleteApi(this.shell, this.ps, org, this.appInfo.getAppId(), control.getException_id());
                exclusionDeleteApi.delete();
                sub2Monitor.worked(1);
                Thread.sleep(sleep);
            }
            sub2Monitor.done();
            Thread.sleep(500);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        monitor.done();
    }
}
