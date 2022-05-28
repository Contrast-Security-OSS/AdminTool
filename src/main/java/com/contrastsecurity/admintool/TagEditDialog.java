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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class TagEditDialog extends Dialog {

    private String tag;
    private List<String> removeTags;
    private Text tagTxt;
    private List<String> existTags;
    private CheckboxTableViewer existTagsViewer;

    public TagEditDialog(Shell parentShell, List<String> existTags) {
        super(parentShell);
        this.existTags = existTags;
        this.removeTags = new ArrayList<String>();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));

        Group srcNameGrp = new Group(composite, SWT.NONE);
        GridLayout srcNameGrpLt = new GridLayout(1, false);
        srcNameGrpLt.marginWidth = 10;
        srcNameGrpLt.marginHeight = 10;
        srcNameGrp.setLayout(srcNameGrpLt);
        GridData srcNameGrpGrDt = new GridData(GridData.FILL_BOTH);
        srcNameGrpGrDt.minimumWidth = 200;
        srcNameGrpGrDt.horizontalSpan = 2;
        srcNameGrp.setLayoutData(srcNameGrpGrDt);
        srcNameGrp.setText("既存タグ");

        new Label(srcNameGrp, SWT.LEFT).setText("削除するタグにチェックをいれてください。");

        final Table srcNameTable = new Table(srcNameGrp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        GridData srcNameTableGrDt = new GridData(GridData.FILL_BOTH);
        srcNameTable.setLayoutData(srcNameTableGrDt);
        existTagsViewer = new CheckboxTableViewer(srcNameTable);
        existTagsViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });
        List<String> srcNameLabelList = new ArrayList<String>();
        for (String existTag : this.existTags) {
            srcNameLabelList.add(existTag);
        }
        existTagsViewer.setContentProvider(new ArrayContentProvider());
        existTagsViewer.setInput(srcNameLabelList);
        existTagsViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                Object[] items = existTagsViewer.getCheckedElements();
                if (items.length > 0) {
                    for (Object item : items) {
                        removeTags.add((String) item);
                    }
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                } else {
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                }
            }
        });

        new Label(composite, SWT.LEFT).setText("タグ：");
        tagTxt = new Text(composite, SWT.BORDER);
        tagTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tagTxt.setMessage("追加するタグはこちらに入力してください。");
        tagTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                tagTxt.selectAll();
            }
        });
        tagTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String tagStr = tagTxt.getText();
                if (tagStr.isEmpty()) {
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                } else {
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                }
            }
        });
        return composite;
    }

    public String getTag() {
        return this.tag;
    }

    public List<String> getRemoveTags() {
        return this.removeTags;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okButton.setEnabled(false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        this.tag = tagTxt.getText();
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(360, 360);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("タグ編集");
    }
}
