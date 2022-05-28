package com.contrastsecurity.admintool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class ExclusionDeleteProgressMonitorDialog extends ProgressMonitorDialog {

    private AppInfo appInfo;

    public ExclusionDeleteProgressMonitorDialog(Shell parent, AppInfo appInfo) {
        super(parent);
        this.appInfo = appInfo;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(String.format("例外の削除 - %s", this.appInfo.getAppName()));
    }

}
