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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.exec.OS;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.SecurityControl;

public class ControlCompareResultDialog extends Dialog {

    private Organization org;
    private List<String> problemStrs;
    private List<SecurityControl> problemControls;
    private Table table;
    private Label diffCount;
    private Button csvExpBtn;

    public ControlCompareResultDialog(Shell parentShell, Organization org, List<String> problemStrs, List<SecurityControl> problemControls) {
        super(parentShell);
        this.org = org;
        this.problemStrs = problemStrs;
        this.problemControls = problemControls;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));

        Label tableTitleLbl = new Label(composite, SWT.LEFT);
        tableTitleLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tableTitleLbl.setText("TeamServer上に存在しないセキュリティ制御の一覧です。");

        Label descLbl = new Label(composite, SWT.LEFT);
        descLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        descLbl.setText("※ セキュリティ制御の名前が一致しないだけでなく、設定内容に差異がある場合も一覧に表示されます。");

        this.diffCount = new Label(composite, SWT.RIGHT);
        GridData srcCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcCountGrDt.horizontalAlignment = SWT.RIGHT;
        this.diffCount.setLayoutData(srcCountGrDt);
        this.diffCount.setText(String.format("%d", problemStrs.size()));

        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(tableGrDt);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableColumn column0 = new TableColumn(table, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(table, SWT.LEFT);
        column1.setWidth(640);
        column1.setText("存在しないセキュリティ制御");

        problemStrs.forEach(str -> addColToTable(str, -1));

        csvExpBtn = new Button(composite, SWT.PUSH);
        GridData csvExpBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        csvExpBtn.setLayoutData(csvExpBtnGrDt);
        csvExpBtn.setText("CSVエクスポート");
        csvExpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(getShell());
                dialog.setText("出力先フォルダを指定してください。");
                String dir = dialog.open();
                if (dir == null) {
                    return;
                }
                String fileName = org.getName() + "_diff.csv";
                List<List<String>> csvList = new ArrayList<List<String>>();
                for (SecurityControl sc : problemControls) {
                    csvList.add(sc.getCsvValues());
                }
                String csv_encoding = Main.CSV_WIN_ENCODING;
                if (OS.isFamilyMac()) {
                    csv_encoding = Main.CSV_MAC_ENCODING;
                }
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(dir + "\\" + fileName)), csv_encoding))) {
                    CSVPrinter printer = CSVFormat.EXCEL.print(bw);
                    String[] headerArray = { "name", "language", "type", "api", "all_rules", "rules" };
                    printer.printRecord(Arrays.asList(headerArray));
                    for (List<String> csvLine : csvList) {
                        printer.printRecord(csvLine);
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                getShell().getDisplay().syncExec(new Runnable() {
                    public void run() {
                        MessageDialog.openInformation(getShell(), "セキュリティ制御のインポート済み確認結果", String.format("CSVファイルを出力しました。\r\n%s", dir + "\\" + fileName));
                    }
                });
            }
        });

        return composite;
    }

    private void addColToTable(String str, int index) {
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(table, SWT.CENTER, index);
        } else {
            item = new TableItem(table, SWT.CENTER);
        }
        item.setText(1, str);
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
        newShell.setText("セキュリティ制御のインポート済み確認結果");
    }
}
