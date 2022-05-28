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

import com.contrastsecurity.admintool.model.SecurityControl;

public class ControlImportResultDialog extends Dialog {

    private List<SecurityControl> successControls;
    private List<SecurityControl> failureControls;
    private Table failedControlsTable;
    private Label successCntLbl;
    private Label failureCntLbl;

    public ControlImportResultDialog(Shell parentShell, List<SecurityControl> successControls, List<SecurityControl> failureControls) {
        super(parentShell);
        this.successControls = successControls;
        this.failureControls = failureControls;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        new Label(composite, SWT.LEFT).setText("成功:");

        this.successCntLbl = new Label(composite, SWT.LEFT);
        GridData successCntLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        this.successCntLbl.setLayoutData(successCntLblGrDt);
        this.successCntLbl.setText(String.format("%d 件", successControls.size()));

        new Label(composite, SWT.LEFT).setText("失敗:");
        this.failureCntLbl = new Label(composite, SWT.LEFT);
        GridData failureCntLblGrDt = new GridData(GridData.FILL_HORIZONTAL);
        this.failureCntLbl.setLayoutData(failureCntLblGrDt);
        this.failureCntLbl.setText(String.format("%d 件", failureControls.size()));

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
        column2.setText("言語");
        TableColumn column3 = new TableColumn(failedControlsTable, SWT.LEFT);
        column3.setWidth(250);
        column3.setText("API");
        TableColumn column4 = new TableColumn(failedControlsTable, SWT.LEFT);
        column4.setWidth(300);
        column4.setText("備考");

        failureControls.forEach(sc -> addColToControlTable(sc, -1));

        return composite;
    }

    private void addColToControlTable(SecurityControl control, int index) {
        if (control == null) {
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
        text.setText(control.getName());
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
        // item.setText(1, exclusion.getName());
        // item.setText(2, control.getLanguage());
        // item.setText(3, control.getApi());
        // item.setText(4, control.getRemarks());
        // item.setText(1, control.getName());
        item.setText(2, control.getLanguage());
        item.setText(3, control.getApi());
        item.setText(4, control.getRemarks());
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "閉じる", true);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(640, 480);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("セキュリティ制御のインポート結果");
    }
}
