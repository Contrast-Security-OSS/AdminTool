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
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.admintool.model.Exclusion;

public class ExclusionImportResultDialog extends Dialog {

    private AppInfo appInfo;
    private List<Exclusion> successExclusions;
    private List<Exclusion> failureExclusions;
    private Table failedControlsTable;
    private Label successCntLbl;
    private Label failureCntLbl;

    public ExclusionImportResultDialog(Shell parentShell, AppInfo appInfo, List<Exclusion> successExclusions, List<Exclusion> failureExclusions) {
        super(parentShell);
        this.appInfo = appInfo;
        this.successExclusions = successExclusions;
        this.failureExclusions = failureExclusions;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        new Label(composite, SWT.LEFT).setText("成功:");

        this.successCntLbl = new Label(composite, SWT.LEFT);
        GridData successCntLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        this.successCntLbl.setLayoutData(successCntLblGrDt);
        this.successCntLbl.setText(String.format("%d 件", successExclusions.size()));

        new Label(composite, SWT.LEFT).setText("失敗:");
        this.failureCntLbl = new Label(composite, SWT.LEFT);
        GridData failureCntLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        this.failureCntLbl.setLayoutData(failureCntLblGrDt);
        this.failureCntLbl.setText(String.format("%d 件", failureExclusions.size()));

        Label tableTitleLbl = new Label(composite, SWT.LEFT);
        GridData tableTitleLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        tableTitleLblGrDt.horizontalSpan = 2;
        tableTitleLbl.setLayoutData(tableTitleLblGrDt);
        tableTitleLbl.setText("インポートに失敗したデータは下のリストに表示されます。");

        failedControlsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        tableGrDt.horizontalSpan = 2;
        failedControlsTable.setLayoutData(tableGrDt);
        failedControlsTable.setLinesVisible(true);
        failedControlsTable.setHeaderVisible(true);
        TableColumn column0 = new TableColumn(failedControlsTable, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(failedControlsTable, SWT.LEFT);
        column1.setWidth(400);
        column1.setText("名前");
        TableColumn column2 = new TableColumn(failedControlsTable, SWT.CENTER);
        column2.setWidth(100);
        column2.setText("種類");
        TableColumn column3 = new TableColumn(failedControlsTable, SWT.CENTER);
        column3.setWidth(200);
        column3.setText("Assessルール");
        TableColumn column4 = new TableColumn(failedControlsTable, SWT.CENTER);
        column4.setWidth(200);
        column4.setText("Protectルール");
        TableColumn column5 = new TableColumn(failedControlsTable, SWT.LEFT);
        column5.setWidth(250);
        column5.setText("備考");

        failureExclusions.forEach(sc -> addColToControlTable(sc, -1));

        return composite;
    }

    private void addColToControlTable(Exclusion exclusion, int index) {
        if (exclusion == null) {
            return;
        }
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(failedControlsTable, SWT.CENTER, index);
        } else {
            item = new TableItem(failedControlsTable, SWT.CENTER);
        }
        TableEditor editor = new TableEditor(failedControlsTable);

        Text text = new Text(failedControlsTable, SWT.SINGLE);
        text.setEditable(false);
        text.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setText(exclusion.getName());
        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                text.selectAll();
            }
        });
        text.pack();
        editor.grabHorizontal = true;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(text, item, 1);
        item.setText(2, exclusion.getType());
        if (exclusion.isAll_rules()) {
            item.setText(3, "ALL");
            item.setText(4, "ALL");
        } else {
            if (exclusion.isAll_assessment_rules() || exclusion.getAssessment_rules().isEmpty()) {
                item.setText(3, "ALL");
            } else {
                item.setText(3, String.join(", ", exclusion.getAssessment_rules().stream().map(r -> r.getName()).collect(Collectors.toList())));
            }
            if (exclusion.isAll_protection_rules() || exclusion.getProtection_rules().isEmpty()) {
                item.setText(4, "ALL");
            } else {
                item.setText(4, String.join(", ", exclusion.getProtection_rules().stream().map(r -> r.getUuid()).collect(Collectors.toList())));
            }
        }
        item.setText(5, exclusion.getRemarks());
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "閉じる", true);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(720, 480);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(String.format("例外のインポート結果 - %s", this.appInfo.getAppName()));
    }
}
