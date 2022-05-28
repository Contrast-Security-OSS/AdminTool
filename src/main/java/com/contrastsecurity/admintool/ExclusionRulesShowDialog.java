/*
 * MIT License
 * Copyright (c) 2015-2019 Tabocom
 *
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
 */

package com.contrastsecurity.admintool;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.admintool.model.AssessRulesConfiguration;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.ProtectionPolicy;

public class ExclusionRulesShowDialog extends Dialog {

    private Table configTable;
    private Table policyTable;
    private Organization org;
    private AppInfo appInfo;
    private List<AssessRulesConfiguration> configs;
    private List<ProtectionPolicy> policies;
    private CTabFolder mainTabFolder;

    public ExclusionRulesShowDialog(Shell parentShell, Organization org, AppInfo appInfo, List<AssessRulesConfiguration> configs, List<ProtectionPolicy> policies) {
        super(parentShell);
        this.org = org;
        this.appInfo = appInfo;
        this.configs = configs;
        this.policies = policies;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));

        mainTabFolder = new CTabFolder(composite, SWT.NONE);
        GridData mainTabFolderGrDt = new GridData(GridData.FILL_BOTH);
        mainTabFolder.setLayoutData(mainTabFolderGrDt);
        mainTabFolder.setSelectionBackground(
                new Color[] { parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
                new int[] { 100 }, true);
        mainTabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });

        // #################### ASSESS #################### //
        CTabItem configTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        configTabItem.setText("ASSESSルール");

        Composite configShell = new Composite(mainTabFolder, SWT.NONE);
        configShell.setLayout(new GridLayout(1, false));

        configTable = new Table(configShell, SWT.BORDER);
        GridData configTableGrDt = new GridData(GridData.FILL_BOTH);
        configTable.setLayoutData(configTableGrDt);
        configTable.setLinesVisible(true);
        configTable.setHeaderVisible(true);
        TableColumn c_col0 = new TableColumn(configTable, SWT.NONE);
        c_col0.setWidth(0);
        c_col0.setResizable(false);
        TableColumn c_col1 = new TableColumn(configTable, SWT.LEFT);
        c_col1.setWidth(400);
        c_col1.setText("日本語名");
        TableColumn c_col2 = new TableColumn(configTable, SWT.LEFT);
        c_col2.setWidth(300);
        c_col2.setText("ルール設定値");
        this.configs.forEach(c -> addColConfigTable(c));

        configTabItem.setControl(configShell);

        // #################### PROTECT #################### //
        CTabItem policyTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        policyTabItem.setText("PROTECTルール");

        Composite policyShell = new Composite(mainTabFolder, SWT.NONE);
        policyShell.setLayout(new GridLayout(1, false));

        policyTable = new Table(policyShell, SWT.BORDER);
        GridData policyTableGrDt = new GridData(GridData.FILL_BOTH);
        policyTable.setLayoutData(policyTableGrDt);
        policyTable.setLinesVisible(true);
        policyTable.setHeaderVisible(true);
        TableColumn p_col0 = new TableColumn(policyTable, SWT.NONE);
        p_col0.setWidth(0);
        p_col0.setResizable(false);
        TableColumn p_col1 = new TableColumn(policyTable, SWT.LEFT);
        p_col1.setWidth(400);
        p_col1.setText("日本語名");
        TableColumn p_col2 = new TableColumn(policyTable, SWT.LEFT);
        p_col2.setWidth(300);
        p_col2.setText("ルール設定値");
        this.policies.forEach(p -> addColPolicyTable(p));

        policyTabItem.setControl(policyShell);

        return composite;
    }

    private void addColConfigTable(AssessRulesConfiguration config) {
        if (config.getRule_name() == null || config.getRule_title() == null) {
            return;
        }
        TableItem item = new TableItem(configTable, SWT.LEFT);
        item.setText(1, config.getRule_title());
        TableEditor editor = new TableEditor(configTable);
        Text text = new Text(configTable, SWT.SINGLE);
        text.setEditable(false);
        text.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setText(config.getRule_name());
        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                text.selectAll();
            }
        });
        text.pack();
        editor.grabHorizontal = true;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(text, item, 2);
    }

    private void addColPolicyTable(ProtectionPolicy policy) {
        if (policy.getName() == null || policy.getUuid() == null) {
            return;
        }
        TableItem item = new TableItem(policyTable, SWT.LEFT);
        item.setText(1, policy.getName());
        TableEditor editor = new TableEditor(policyTable);
        Text text = new Text(policyTable, SWT.SINGLE);
        text.setEditable(false);
        text.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setText(policy.getUuid());
        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                text.selectAll();
            }
        });
        text.pack();
        editor.grabHorizontal = true;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(text, item, 2);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(720, 560);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(String.format("ルール一覧 - %s", this.appInfo.getAppName()));
    }
}
