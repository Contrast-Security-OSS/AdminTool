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

package com.contrastsecurity.admintool.preference;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.admintool.api.Api;
import com.contrastsecurity.admintool.api.OrganizationApi;
import com.contrastsecurity.admintool.exception.ApiException;
import com.contrastsecurity.admintool.exception.NonApiException;
import com.contrastsecurity.admintool.exception.TsvException;
import com.contrastsecurity.admintool.model.Organization;

public class OrganizationDialog extends Dialog {

    private IPreferenceStore ps;
    private String url;
    private String usr;
    private String svc;
    private Text orgIdTxt;
    private Text apiKeyTxt;

    private Organization org;

    public OrganizationDialog(Shell parentShell, IPreferenceStore ps, String url, String usr, String svc) {
        super(parentShell);
        this.ps = ps;
        this.url = url;
        this.usr = usr;
        this.svc = svc;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        new Label(composite, SWT.LEFT).setText("組織ID：");
        orgIdTxt = new Text(composite, SWT.BORDER);
        orgIdTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        orgIdTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                orgIdTxt.selectAll();
            }
        });
        orgIdTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String orgStr = orgIdTxt.getText().trim();
                String apikeyStr = apiKeyTxt.getText().trim();
                if (orgStr.isEmpty() || apikeyStr.isEmpty()) {
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                } else {
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                }
            }
        });
        orgIdTxt.setFocus();
        new Label(composite, SWT.LEFT).setText("API Key：");
        apiKeyTxt = new Text(composite, SWT.BORDER);
        apiKeyTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        apiKeyTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                apiKeyTxt.selectAll();
            }
        });
        apiKeyTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String orgStr = orgIdTxt.getText().trim();
                String apikeyStr = apiKeyTxt.getText().trim();
                if (orgStr.isEmpty() || apikeyStr.isEmpty()) {
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                } else {
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                }
            }
        });
        return composite;
    }

    public Organization getOrg() {
        return org;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okButton.setEnabled(false);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        Organization org = new Organization();
        org.setApikey(apiKeyTxt.getText().trim());
        org.setOrganization_uuid(orgIdTxt.getText().trim());
        org.setValid(false);
        Api orgApi = new OrganizationApi(getShell(), this.ps, org, url, usr, svc);
        try {
            Organization rtnOrg = (Organization) orgApi.get();
            org.setName(rtnOrg.getName());
            this.org = org;
        } catch (ApiException e) {
            MessageDialog.openWarning(getShell(), "組織情報の確認", String.format("TeamServerからエラーが返されました。\r\n%s", e.getMessage()));
        } catch (NonApiException e) {
            MessageDialog.openError(getShell(), "組織情報の確認", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", e.getMessage()));
        } catch (TsvException e) {
            MessageDialog.openError(getShell(), "組織情報の確認", e.getMessage());
        } catch (Exception e) {
            MessageDialog.openError(getShell(), "組織情報の確認", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", e.getMessage()));
        }
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(480, 150);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("組織の追加");
    }
}
