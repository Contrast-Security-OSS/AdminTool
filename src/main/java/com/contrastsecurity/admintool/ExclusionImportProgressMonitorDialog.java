package com.contrastsecurity.admintool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class ExclusionImportProgressMonitorDialog extends ProgressMonitorDialog {

    private AppInfo appInfo;

    public ExclusionImportProgressMonitorDialog(Shell parent, AppInfo appInfo) {
        super(parent);
        this.appInfo = appInfo;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(String.format("例外のインポート - %s", this.appInfo.getAppName()));
    }

}
