package com.contrastsecurity.admintool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class ExclusionCompareProgressMonitorDialog extends ProgressMonitorDialog {

    private AppInfo appInfo;

    public ExclusionCompareProgressMonitorDialog(Shell parent, AppInfo appInfo) {
        super(parent);
        this.appInfo = appInfo;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(String.format("例外のインポート済み確認 - %s", this.appInfo.getAppName()));
    }

}
