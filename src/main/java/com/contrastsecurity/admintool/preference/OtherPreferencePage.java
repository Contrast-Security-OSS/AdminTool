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

package com.contrastsecurity.admintool.preference;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class OtherPreferencePage extends PreferencePage {

    private Text scDelSleepTxt;
    private Text scImpSleepTxt;

    private Text exDelSleepTxt;
    private Text exImpSleepTxt;

    public OtherPreferencePage() {
        super("その他設定");
    }

    @Override
    protected Control createContents(Composite parent) {
        IPreferenceStore ps = getPreferenceStore();

        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginHeight = 15;
        compositeLt.marginWidth = 5;
        compositeLt.horizontalSpacing = 10;
        compositeLt.verticalSpacing = 20;
        composite.setLayout(compositeLt);

        Group ctrlGrp = new Group(composite, SWT.NONE);
        GridLayout proxyGrpLt = new GridLayout(1, false);
        proxyGrpLt.marginWidth = 15;
        proxyGrpLt.horizontalSpacing = 10;
        proxyGrpLt.verticalSpacing = 10;
        ctrlGrp.setLayout(proxyGrpLt);
        GridData proxyGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // proxyGrpGrDt.horizontalSpan = 4;
        ctrlGrp.setLayoutData(proxyGrpGrDt);
        ctrlGrp.setText("スリープ設定");

        Label descLbl = new Label(ctrlGrp, SWT.LEFT);
        descLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        descLbl.setText("APIを発行する間隔をミリ秒単位で指定できます。タイムアウトが発生する場合や、TeamServer側の負荷に応じて調整してください。");

        Group scGrp = new Group(ctrlGrp, SWT.NONE);
        GridLayout scGrpLt = new GridLayout(2, false);
        scGrpLt.marginWidth = 15;
        scGrpLt.horizontalSpacing = 10;
        scGrp.setLayout(scGrpLt);
        GridData scGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        scGrp.setLayoutData(scGrpGrDt);
        scGrp.setText("セキュリティ制御");

        // ========== 削除間隔スリープ ========== //
        new Label(scGrp, SWT.LEFT).setText("削除：");
        scDelSleepTxt = new Text(scGrp, SWT.BORDER);
        scDelSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        scDelSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_SC_DEL));
        scDelSleepTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                scDelSleepTxt.selectAll();
            }
        });

        // ========== 登録間隔スリープ ========== //
        new Label(scGrp, SWT.LEFT).setText("登録：");
        scImpSleepTxt = new Text(scGrp, SWT.BORDER);
        scImpSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        scImpSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_SC_IMP));
        scImpSleepTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                scImpSleepTxt.selectAll();
            }
        });

        Group exGrp = new Group(ctrlGrp, SWT.NONE);
        GridLayout exGrpLt = new GridLayout(2, false);
        exGrpLt.marginWidth = 15;
        exGrpLt.horizontalSpacing = 10;
        exGrp.setLayout(exGrpLt);
        GridData exGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // exGrpGrDt.horizontalSpan = 4;
        exGrp.setLayoutData(exGrpGrDt);
        exGrp.setText("例外");

        // ========== 削除間隔スリープ ========== //
        new Label(exGrp, SWT.LEFT).setText("削除：");
        exDelSleepTxt = new Text(exGrp, SWT.BORDER);
        exDelSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        exDelSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_EX_DEL));
        exDelSleepTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                exDelSleepTxt.selectAll();
            }
        });

        // ========== 登録間隔スリープ ========== //
        new Label(exGrp, SWT.LEFT).setText("登録：");
        exImpSleepTxt = new Text(exGrp, SWT.BORDER);
        exImpSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        exImpSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_EX_IMP));
        exImpSleepTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                exImpSleepTxt.selectAll();
            }
        });

        Composite buttonGrp = new Composite(parent, SWT.NONE);
        GridLayout buttonGrpLt = new GridLayout(2, false);
        buttonGrpLt.marginHeight = 15;
        buttonGrpLt.marginWidth = 5;
        buttonGrpLt.horizontalSpacing = 7;
        buttonGrpLt.verticalSpacing = 20;
        buttonGrp.setLayout(buttonGrpLt);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        buttonGrpGrDt.horizontalAlignment = SWT.END;
        buttonGrp.setLayoutData(buttonGrpGrDt);

        Button defaultBtn = new Button(buttonGrp, SWT.NULL);
        GridData defaultBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        defaultBtnGrDt.widthHint = 90;
        defaultBtn.setLayoutData(defaultBtnGrDt);
        defaultBtn.setText("デフォルトに戻す");
        defaultBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                scDelSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_SC_DEL));
                scImpSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_SC_IMP));
                exDelSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_EX_DEL));
                exImpSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_EX_IMP));
            }
        });

        Button applyBtn = new Button(buttonGrp, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText("適用");
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                performOk();
            }
        });

        noDefaultAndApplyButton();
        return composite;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        List<String> errors = new ArrayList<String>();
        // セキュリティ制御チェック
        if (this.scDelSleepTxt.getText().isEmpty()) {
            errors.add("・セキュリティ制御の削除間隔スリープを指定してください。");
        } else {
            if (!StringUtils.isNumeric(this.scDelSleepTxt.getText())) {
                errors.add("・セキュリティ制御の削除間隔スリープは数値を指定してください。");
            }
        }
        if (this.scImpSleepTxt.getText().isEmpty()) {
            errors.add("・セキュリティ制御の登録間隔スリープを指定してください。");
        } else {
            if (!StringUtils.isNumeric(this.scImpSleepTxt.getText())) {
                errors.add("・セキュリティ制御の登録間隔スリープは数値を指定してください。");
            }
        }
        // 例外チェック
        if (this.exDelSleepTxt.getText().isEmpty()) {
            errors.add("・例外の削除間隔スリープを指定してください。");
        } else {
            if (!StringUtils.isNumeric(this.exDelSleepTxt.getText())) {
                errors.add("・例外の削除間隔スリープは数値を指定してください。");
            }
        }
        if (this.exImpSleepTxt.getText().isEmpty()) {
            errors.add("・例外の登録間隔スリープを指定してください。");
        } else {
            if (!StringUtils.isNumeric(this.exImpSleepTxt.getText())) {
                errors.add("・例外の登録間隔スリープは数値を指定してください。");
            }
        }
        ps.setValue(PreferenceConstants.SLEEP_SC_DEL, this.scDelSleepTxt.getText());
        ps.setValue(PreferenceConstants.SLEEP_SC_IMP, this.scImpSleepTxt.getText());
        ps.setValue(PreferenceConstants.SLEEP_EX_DEL, this.exDelSleepTxt.getText());
        ps.setValue(PreferenceConstants.SLEEP_EX_IMP, this.exImpSleepTxt.getText());
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "その他設定", String.join("\r\n", errors));
            return false;
        }
        return true;
    }
}
