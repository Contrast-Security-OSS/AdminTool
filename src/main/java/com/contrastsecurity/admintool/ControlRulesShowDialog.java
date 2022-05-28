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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.Rule;

public class ControlRulesShowDialog extends Dialog {

    private Table table;
    private Organization org;
    private List<Rule> rules;
    private List<StyledText> styledTexts;

    public ControlRulesShowDialog(Shell parentShell, Organization org, List<Rule> rules) {
        super(parentShell);
        this.org = org;
        this.rules = rules;
        this.styledTexts = new ArrayList<StyledText>();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));
        table = new Table(composite, SWT.BORDER);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(tableGrDt);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableColumn column0 = new TableColumn(table, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(table, SWT.LEFT);
        column1.setWidth(250);
        column1.setText("日本語名");
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(250);
        column2.setText("ルール設定値");
        TableColumn column3 = new TableColumn(table, SWT.LEFT);
        column3.setWidth(350);
        column3.setText("対応言語");
        this.rules.forEach(r -> addColTable(r));
        return composite;
    }

    private void addColTable(Rule rule) {
        TableItem item = new TableItem(table, SWT.LEFT);
        item.setText(1, rule.getTitle());
        TableEditor editor2 = new TableEditor(table);
        TableEditor editor3 = new TableEditor(table);
        StyledText text1 = new StyledText(table, SWT.SINGLE);
        text1.setEditable(false);
        text1.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
        text1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text1.setText(rule.getName());
        text1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                styledTexts.forEach(st -> st.setSelectionRange(0, 0));
                text1.selectAll();
            }
        });
        text1.pack();
        styledTexts.add(text1);
        editor2.grabHorizontal = true;
        editor2.horizontalAlignment = SWT.LEFT;
        editor2.setEditor(text1, item, 2);

        StyledText text2 = new StyledText(table, SWT.SINGLE);
        text2.setEditable(false);
        text2.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
        text2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text2.setText(String.join(",", rule.getLanguages()));
        text2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Point p = text2.getSelectionRange();
                Point p2 = text2.getSelection();
                int max = text2.getText().length();
                int x = p.x;
                int y = p2.y;
                while (x > 0) {
                    String s = text2.getTextRange(x, 1);
                    if (s.equals(",")) {
                        x++;
                        break;
                    }
                    x--;
                }
                while (y < max) {
                    String s = text2.getTextRange(y, 1);
                    if (s.equals(",")) {
                        break;
                    }
                    y++;
                }
                styledTexts.forEach(st -> st.setSelectionRange(0, 0));
                text2.setSelection(x, y);
            }
        });
        text2.pack();
        styledTexts.add(text2);
        editor3.grabHorizontal = true;
        editor3.horizontalAlignment = SWT.LEFT;
        editor3.setEditor(text2, item, 3);
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
        newShell.setText(String.format("ルール一覧 - %s", this.org.getName()));
    }
}
