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
import com.contrastsecurity.admintool.api.ControlDeleteApi;
import com.contrastsecurity.admintool.api.ControlsApi;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.SecurityControl;
import com.contrastsecurity.admintool.preference.PreferenceConstants;

public class ControlDeleteWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private Organization org;
    private String filterWord;
    private List<SecurityControl> targetControls;

    Logger logger = LogManager.getLogger("admintool");

    public ControlDeleteWithProgress(Shell shell, PreferenceStore ps, Organization org, String filterWord) {
        this.shell = shell;
        this.ps = ps;
        this.org = org;
        this.filterWord = filterWord;
        this.targetControls = new ArrayList<SecurityControl>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("セキュリティ制御の削除...", 100);
        Thread.sleep(300);
        int sleep = this.ps.getInt(PreferenceConstants.SLEEP_SC_DEL);
        try {
            monitor.subTask("セキュリティ制御の情報を取得...");
            SubProgressMonitor sub1Monitor = new SubProgressMonitor(monitor, 100);
            Api controlsApi = new ControlsApi(this.shell, this.ps, org);
            List<SecurityControl> controls = (List<SecurityControl>) controlsApi.get();
            sub1Monitor.beginTask("", controls.size());
            for (SecurityControl control : controls) {
                if (monitor.isCanceled()) {
                    throw new InterruptedException("キャンセルされました。");
                }
                monitor.subTask(String.format("セキュリティ制御の情報を取得...%s", control.getName()));
                control.setDeleteFlg(false);
                if (!filterWord.isEmpty()) {
                    if (filterWord.contains("*")) {
                        String word = filterWord.replace("*", "");
                        if (filterWord.startsWith("*") && filterWord.endsWith("*")) {
                            if (control.getName().contains(word)) {
                                control.setDeleteFlg(true);
                            }
                        } else if (filterWord.endsWith("*")) {
                            if (control.getName().startsWith(word)) {
                                control.setDeleteFlg(true);
                            }
                        } else if (filterWord.startsWith("*")) {
                            if (control.getName().endsWith(word)) {
                                control.setDeleteFlg(true);
                            }
                        }
                    } else {
                        if (control.getName().equals(filterWord)) {
                            control.setDeleteFlg(true);
                        }
                    }
                } else {
                    control.setDeleteFlg(true);
                }
                this.targetControls.add(control);
                sub1Monitor.worked(1);
                Thread.sleep(10);
            }
            sub1Monitor.done();

            ControlDeleteConfirmDialog dialog = new ControlDeleteConfirmDialog(shell, this.targetControls);
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
            monitor.beginTask("セキュリティ制御の削除...", 100);
            SubProgressMonitor sub2Monitor = new SubProgressMonitor(monitor, 100);
            sub2Monitor.beginTask("", selectedIdxes.size());
            int cnt = 1;
            for (Integer index : selectedIdxes) {
                if (monitor.isCanceled()) {
                    return;
                }
                SecurityControl control = this.targetControls.get(index);
                monitor.subTask(String.format("セキュリティ制御を削除...%s (%d/%d)", control.getName(), cnt++, selectedIdxes.size()));
                Api controlDeleteApi = new ControlDeleteApi(this.shell, this.ps, org, control.getId());
                controlDeleteApi.delete();
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
