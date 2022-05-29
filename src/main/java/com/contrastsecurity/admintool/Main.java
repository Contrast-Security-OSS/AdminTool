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

package com.contrastsecurity.admintool;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.exec.OS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.yaml.snakeyaml.Yaml;

import com.contrastsecurity.admintool.api.Api;
import com.contrastsecurity.admintool.api.AssessRulesConfigurationsApi;
import com.contrastsecurity.admintool.api.ProtectionPoliciesApi;
import com.contrastsecurity.admintool.api.RulesApi;
import com.contrastsecurity.admintool.exception.ApiException;
import com.contrastsecurity.admintool.exception.NonApiException;
import com.contrastsecurity.admintool.exception.TsvException;
import com.contrastsecurity.admintool.model.AssessRulesConfiguration;
import com.contrastsecurity.admintool.model.ContrastSecurityYaml;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.ProtectionPolicy;
import com.contrastsecurity.admintool.model.Rule;
import com.contrastsecurity.admintool.preference.AboutPage;
import com.contrastsecurity.admintool.preference.BasePreferencePage;
import com.contrastsecurity.admintool.preference.ConnectionPreferencePage;
import com.contrastsecurity.admintool.preference.MyPreferenceDialog;
import com.contrastsecurity.admintool.preference.OtherPreferencePage;
import com.contrastsecurity.admintool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Main implements PropertyChangeListener {

    public static final String WINDOW_TITLE = "AdminTool - %s";
    // 以下のMASTER_PASSWORDはプロキシパスワードを保存する際に暗号化で使用するパスワードです。
    // 本ツールをリリース用にコンパイルする際はchangemeを別の文字列に置き換えてください。
    public static final String MASTER_PASSWORD = "changeme!";

    public static final int MINIMUM_SIZE_WIDTH = 640;
    public static final int MINIMUM_SIZE_HEIGHT = 640;

    private AdminToolShell shell;

    private Organization currentOrg;

    private List<Button> actionBtns;
    private CTabFolder mainTabFolder;

    private Button scExpBtn;
    private Button scDelBtn;
    private Text scFilterWordTxt;
    private Button scImpBtn;
    private Button scCmpBtn;
    private Button scSklBtn;
    private Button scRulesShowBtn;

    private Button appLoadBtn;
    private Text srcListFilter;
    private Text dstListFilter;
    private org.eclipse.swt.widgets.List srcList;
    private org.eclipse.swt.widgets.List dstList;
    private Label srcCount;
    private Label dstCount;
    private Map<String, AppInfo> fullAppMap;
    private List<String> srcApps = new ArrayList<String>();
    private List<String> dstApps = new ArrayList<String>();

    private Button exExpBtn;
    private Button exDelBtn;
    private Text exFilterWordTxt;
    private Text exImpRepBefWordTxt;
    private Text exImpRepAftWordTxt;
    private Button exImpBtn;
    private Button exCmpBtn;
    private Button exSklBtn;
    private Button exRulesShowBtn;

    private Button settingBtn;

    private PreferenceStore ps;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    Logger logger = LogManager.getLogger("admintool");

    /**
     * @param args
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.initialize();
        main.createPart();
    }

    private void initialize() {
        this.actionBtns = new ArrayList<Button>();
        try {
            String homeDir = System.getProperty("user.home");
            this.ps = new PreferenceStore(homeDir + "\\admintool.properties");
            if (OS.isFamilyMac()) {
                this.ps = new PreferenceStore(homeDir + "/admintool.properties");
            }
            try {
                this.ps.load();
            } catch (FileNotFoundException fnfe) {
                this.ps = new PreferenceStore("admintool.properties");
                this.ps.load();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.ps.setDefault(PreferenceConstants.TSV_STATUS, TsvStatusEnum.NONE.name());
            this.ps.setDefault(PreferenceConstants.PROXY_AUTH, "none");
            this.ps.setDefault(PreferenceConstants.CONNECTION_TIMEOUT, 3000);
            this.ps.setDefault(PreferenceConstants.SOCKET_TIMEOUT, 3000);

            this.ps.setDefault(PreferenceConstants.SLEEP_SC_DEL, 100);
            this.ps.setDefault(PreferenceConstants.SLEEP_SC_IMP, 100);
            this.ps.setDefault(PreferenceConstants.SLEEP_EX_DEL, 100);
            this.ps.setDefault(PreferenceConstants.SLEEP_EX_IMP, 100);

            this.ps.setDefault(PreferenceConstants.OPENED_MAIN_TAB_IDX, 0);

            Yaml yaml = new Yaml();
            InputStream is = new FileInputStream("contrast_security.yaml");
            ContrastSecurityYaml contrastSecurityYaml = yaml.loadAs(is, ContrastSecurityYaml.class);
            is.close();
            this.ps.setDefault(PreferenceConstants.CONTRAST_URL, contrastSecurityYaml.getUrl());
            this.ps.setDefault(PreferenceConstants.SERVICE_KEY, contrastSecurityYaml.getServiceKey());
            this.ps.setDefault(PreferenceConstants.USERNAME, contrastSecurityYaml.getUserName());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private void createPart() {
        Display display = new Display();
        shell = new AdminToolShell(display, this);
        shell.setMinimumSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
        Image[] imageArray = new Image[5];
        imageArray[0] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon16.png"));
        imageArray[1] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon24.png"));
        imageArray[2] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon32.png"));
        imageArray[3] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon48.png"));
        imageArray[4] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon128.png"));
        shell.setImages(imageArray);
        Window.setDefaultImages(imageArray);
        setWindowTitle();
        shell.addShellListener(new ShellListener() {
            @Override
            public void shellIconified(ShellEvent event) {
            }

            @Override
            public void shellDeiconified(ShellEvent event) {
            }

            @Override
            public void shellDeactivated(ShellEvent event) {
            }

            @Override
            public void shellClosed(ShellEvent event) {
                int main_idx = mainTabFolder.getSelectionIndex();
                ps.setValue(PreferenceConstants.OPENED_MAIN_TAB_IDX, main_idx);
                ps.setValue(PreferenceConstants.MEM_WIDTH, shell.getSize().x);
                ps.setValue(PreferenceConstants.MEM_HEIGHT, shell.getSize().y);
                ps.setValue(PreferenceConstants.CONTROL_DEL_FILTER_WORD, scFilterWordTxt.getText());
                ps.setValue(PreferenceConstants.EXCLUSION_DEL_FILTER_WORD, exFilterWordTxt.getText());
                ps.setValue(PreferenceConstants.EXCLUSION_IMP_REP_BEF_WORD, exImpRepBefWordTxt.getText());
                ps.setValue(PreferenceConstants.EXCLUSION_IMP_REP_AFT_WORD, exImpRepAftWordTxt.getText());
                ps.setValue(PreferenceConstants.PROXY_TMP_USER, "");
                ps.setValue(PreferenceConstants.PROXY_TMP_PASS, "");
                ps.setValue(PreferenceConstants.TSV_STATUS, "");
                try {
                    ps.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            @Override
            public void shellActivated(ShellEvent event) {
                Organization org = getValidOrganization();
                if (org == null) {
                    actionBtns.forEach(b -> b.setEnabled(false));
                    settingBtn.setText("このボタンから基本設定を行ってください。");
                    uiReset();
                } else {
                    if (currentOrg != null && !currentOrg.equals(org)) {
                        uiReset();
                    }
                    currentOrg = org;
                    actionBtns.forEach(b -> b.setEnabled(true));
                    uiUpdateForExclusionButton();
                    settingBtn.setText("設定");
                }
                setWindowTitle();
                if (ps.getBoolean(PreferenceConstants.PROXY_YUKO) && ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) {
                    String usr = ps.getString(PreferenceConstants.PROXY_TMP_USER);
                    String pwd = ps.getString(PreferenceConstants.PROXY_TMP_PASS);
                    if (usr == null || usr.isEmpty() || pwd == null || pwd.isEmpty()) {
                        ProxyAuthDialog proxyAuthDialog = new ProxyAuthDialog(shell);
                        int result = proxyAuthDialog.open();
                        if (IDialogConstants.CANCEL_ID == result) {
                            ps.setValue(PreferenceConstants.PROXY_AUTH, "none");
                        } else {
                            ps.setValue(PreferenceConstants.PROXY_TMP_USER, proxyAuthDialog.getUsername());
                            ps.setValue(PreferenceConstants.PROXY_TMP_PASS, proxyAuthDialog.getPassword());
                        }
                    }
                }
            }
        });

        Listener listener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.stateMask == SWT.CTRL) {
                    int num = Character.getNumericValue(event.character);
                    if (num > -1) {
                        support.firePropertyChange("userswitch", 0, num);
                    }
                }
            }
        };
        display.addFilter(SWT.KeyUp, listener);

        GridLayout baseLayout = new GridLayout(1, false);
        baseLayout.marginWidth = 8;
        baseLayout.marginBottom = 8;
        baseLayout.verticalSpacing = 8;
        shell.setLayout(baseLayout);

        mainTabFolder = new CTabFolder(shell, SWT.NONE);
        GridData mainTabFolderGrDt = new GridData(GridData.FILL_BOTH);
        mainTabFolder.setLayoutData(mainTabFolderGrDt);
        mainTabFolder.setSelectionBackground(new Color[] { display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
                new int[] { 100 }, true);
        mainTabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });

        // #################### セキュリティ制御 #################### //
        CTabItem scTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        scTabItem.setText("セキュリティ制御");

        Composite scShell = new Composite(mainTabFolder, SWT.NONE);
        scShell.setLayout(new GridLayout(1, false));

        // ========== グループ ==========
        Composite scBtnGrp = new Composite(scShell, SWT.NULL);
        GridLayout scBtnGrpLt = new GridLayout(2, false);
        scBtnGrpLt.marginWidth = 10;
        scBtnGrpLt.marginHeight = 10;
        scBtnGrp.setLayout(scBtnGrpLt);
        GridData scBtnGrpGrDt = new GridData(GridData.FILL_BOTH);
        // buttonGrpGrDt.horizontalSpan = 3;
        // buttonGrpGrDt.widthHint = 100;
        scBtnGrp.setLayoutData(scBtnGrpGrDt);

        // ========== エクスポートボタン ==========
        scExpBtn = new Button(scBtnGrp, SWT.PUSH);
        GridData scExpBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        scExpBtnGrDt.heightHint = 30;
        scExpBtnGrDt.horizontalSpan = 2;
        scExpBtn.setLayoutData(scExpBtnGrDt);
        scExpBtn.setText("エクスポート");
        scExpBtn.setToolTipText("セキュリティ制御のエクスポート");
        scExpBtn.setFont(new Font(display, "ＭＳ ゴシック", 13, SWT.NORMAL));
        actionBtns.add(scExpBtn);
        scExpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText("出力先フォルダを指定してください。");
                String dir = dialog.open();
                if (dir == null) {
                    return;
                }
                ControlExportWithProgress progress = new ControlExportWithProgress(shell, ps, getValidOrganization(), dir);
                ProgressMonitorDialog progDialog = new ControlExportProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "セキュリティ制御のエクスポート", String.format("TeamServerからエラーが返されました。\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "セキュリティ制御のエクスポート", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "セキュリティ制御のエクスポート", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "セキュリティ制御のエクスポート", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // ========== 削除ボタン ==========
        Composite scDelBtnGrp = new Composite(scBtnGrp, SWT.NULL);
        GridLayout scDelBtnGrpLt = new GridLayout(2, false);
        scDelBtnGrpLt.marginWidth = 0;
        scDelBtnGrpLt.marginHeight = 0;
        scDelBtnGrp.setLayout(scDelBtnGrpLt);
        GridData scDelBtnGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        scDelBtnGrpGrDt.horizontalSpan = 2;
        // deleteGrpGrDt.heightHint = 140;
        scDelBtnGrp.setLayoutData(scDelBtnGrpGrDt);

        scDelBtn = new Button(scDelBtnGrp, SWT.PUSH);
        GridData scDelBtnGrDt = new GridData(GridData.FILL_BOTH);
        scDelBtn.setLayoutData(scDelBtnGrDt);
        scDelBtn.setText("削除対象を表示");
        scDelBtn.setToolTipText("セキュリティ制御の削除");
        scDelBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        actionBtns.add(scDelBtn);
        scDelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String filterWord = scFilterWordTxt.getText().trim();
                ControlDeleteWithProgress progress = new ControlDeleteWithProgress(shell, ps, getValidOrganization(), filterWord);
                ProgressMonitorDialog progDialog = new ControlDeleteProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "セキュリティ制御のエクスポート", String.format("TeamServerからエラーが返されました。\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "セキュリティ制御のエクスポート", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "セキュリティ制御のエクスポート", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "セキュリティ制御のエクスポート", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Composite scDelCtrlGrp = new Composite(scDelBtnGrp, SWT.NULL);
        GridLayout scDelCtrlGrpLt = new GridLayout(1, false);
        scDelCtrlGrpLt.marginWidth = 0;
        scDelCtrlGrpLt.marginHeight = 1;
        scDelCtrlGrp.setLayout(scDelCtrlGrpLt);
        GridData scDelCtrlGrpGrDt = new GridData(GridData.FILL_BOTH);
        // deleteControlGrpGrDt.heightHint = 70;
        scDelCtrlGrp.setLayoutData(scDelCtrlGrpGrDt);

        scFilterWordTxt = new Text(scDelCtrlGrp, SWT.BORDER);
        scFilterWordTxt.setText(ps.getString(PreferenceConstants.CONTROL_DEL_FILTER_WORD));
        scFilterWordTxt.setMessage("例) hoge, foo_*, *bar*, *_baz");
        scFilterWordTxt.setToolTipText("削除対象を指定します。アスタリスク使用で前方、後方、部分一致を指定できます。カンマ区切りで複数指定可能です。");
        scFilterWordTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        scFilterWordTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                scFilterWordTxt.selectAll();
            }
        });

        // ========== インポートボタン ==========
        scImpBtn = new Button(scBtnGrp, SWT.PUSH);
        GridData scImpBtnGrDt = new GridData(GridData.FILL_BOTH);
        scImpBtnGrDt.heightHint = 50;
        scImpBtnGrDt.horizontalSpan = 2;
        scImpBtn.setLayoutData(scImpBtnGrDt);
        scImpBtn.setText("インポート");
        scImpBtn.setToolTipText("セキュリティ制御のインポート");
        scImpBtn.setFont(new Font(display, "ＭＳ ゴシック", 18, SWT.NORMAL));
        actionBtns.add(scImpBtn);
        scImpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dialog = new FileDialog(shell);
                dialog.setText("インポートするjsonファイルを指定してください。");
                dialog.setFilterExtensions(new String[] { "*.json" });
                String file = dialog.open();
                if (file == null) {
                    return;
                }
                ControlImportWithProgress progress = new ControlImportWithProgress(shell, ps, getValidOrganization(), file);
                ProgressMonitorDialog progDialog = new ControlImportProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // ========== 差分確認ボタン ==========
        scCmpBtn = new Button(scBtnGrp, SWT.PUSH);
        GridData scCmpBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // sanitizerCompareBtnGrDt.heightHint = 30;
        scCmpBtnGrDt.horizontalSpan = 2;
        scCmpBtn.setLayoutData(scCmpBtnGrDt);
        scCmpBtn.setText("インポート済みチェック");
        scCmpBtn.setToolTipText("セキュリティ制御が正しくインポートされているかを確認します。");
        scCmpBtn.setFont(new Font(display, "ＭＳ ゴシック", 13, SWT.NORMAL));
        actionBtns.add(scCmpBtn);
        scCmpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dialog = new FileDialog(shell);
                dialog.setText("比較する対象のjsonファイルを指定してください。");
                dialog.setFilterExtensions(new String[] { "*.json" });
                String file = dialog.open();
                if (file == null) {
                    return;
                }
                ControlCompareWithProgress progress = new ControlCompareWithProgress(shell, ps, getValidOrganization(), file);
                ProgressMonitorDialog progDialog = new ControlCompareProgressMonitorDialog(shell, getValidOrganization());
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // ========== スケルトン生成ボタン ==========
        scSklBtn = new Button(scBtnGrp, SWT.PUSH);
        GridData scSklBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        scSklBtn.setLayoutData(scSklBtnGrDt);
        scSklBtn.setText("スケルトンJSON出力");
        scSklBtn.setToolTipText("セキュリティ制御のインポートJSONファイルのスケルトン生成");
        scSklBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        scSklBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText("出力先フォルダを指定してください。");
                String dir = dialog.open();
                if (dir == null) {
                    return;
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try {
                    String fileName = dir + "\\control_skeleton.json";
                    Writer writer = new FileWriter(fileName);
                    List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("api", "jp.co.contrast.foo(java.lang.String*)");
                    map.put("language", "Java");
                    map.put("name", "Sanitaizer_foo");
                    map.put("type", "SANITIZER");
                    map.put("all_rules", true);
                    mapList.add(map);
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("api", "jp.co.contrast.foo(java.lang.String*)");
                    map2.put("language", "Java");
                    map2.put("name", "Validator_bar");
                    map2.put("type", "INPUT_VALIDATOR");
                    map2.put("all_rules", false);
                    map2.put("rules", Arrays.asList(new String[] { "hql-injection", "sql-injection" }));
                    mapList.add(map2);
                    gson.toJson(mapList, writer);
                    writer.close();
                    MessageDialog.openInformation(shell, "セキュリティ制御のスケルトンJSON出力", String.format("スケルトンJSONファイルを出力しました。\r\n%s", fileName));
                } catch (Exception e) {
                    MessageDialog.openError(shell, "セキュリティ制御のスケルトンJSON出力", e.getMessage());
                }
            }
        });

        scRulesShowBtn = new Button(scBtnGrp, SWT.PUSH);
        scRulesShowBtn.setText("ルール一覧");
        scRulesShowBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        actionBtns.add(scRulesShowBtn);
        scRulesShowBtn.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings({ "unchecked" })
            @Override
            public void widgetSelected(SelectionEvent e) {
                Api rulesApi = new RulesApi(shell, ps, getValidOrganization());
                try {
                    List<Rule> rules = (List<Rule>) rulesApi.get();
                    ControlRulesShowDialog rulesShowDialog = new ControlRulesShowDialog(shell, getValidOrganization(), rules);
                    rulesShowDialog.open();
                } catch (Exception e2) {
                    MessageDialog.openError(shell, "ルール一覧", String.format("エラーが発生しました。ログファイルをご確認ください。\r\n%s", e2.getMessage()));
                }
            }
        });

        scTabItem.setControl(scShell);

        // #################### 例外 #################### //
        CTabItem exTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        exTabItem.setText("例外");

        Composite exShell = new Composite(mainTabFolder, SWT.NONE);
        exShell.setLayout(new GridLayout(1, false));

        // ========== グループ ==========
        Composite exBtnGrp = new Composite(exShell, SWT.NULL);
        GridLayout exBtnGrpLt = new GridLayout(2, false);
        exBtnGrpLt.marginWidth = 10;
        exBtnGrpLt.marginHeight = 0;
        exBtnGrp.setLayout(exBtnGrpLt);
        GridData exBtnGrpGrDt = new GridData(GridData.FILL_BOTH);
        // exButtonGrpGrDt.horizontalSpan = 3;
        // exButtonGrpGrDt.widthHint = 100;
        exBtnGrp.setLayoutData(exBtnGrpGrDt);

        Group appListGrp = new Group(exBtnGrp, SWT.NONE);
        appListGrp.setLayout(new GridLayout(3, false));
        GridData appListGrpGrDt = new GridData(GridData.FILL_BOTH);
        appListGrpGrDt.horizontalSpan = 2;
        appListGrpGrDt.minimumHeight = 200;
        appListGrp.setLayoutData(appListGrpGrDt);

        appLoadBtn = new Button(appListGrp, SWT.PUSH);
        GridData appLoadBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        appLoadBtnGrDt.horizontalSpan = 3;
        appLoadBtn.setLayoutData(appLoadBtnGrDt);
        appLoadBtn.setText("アプリケーション一覧の読み込み");
        appLoadBtn.setToolTipText("TeamServerにオンボードされているアプリケーションを読み込みます。");
        actionBtns.add(appLoadBtn);
        appLoadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                uiReset();

                AppsGetWithProgress progress = new AppsGetWithProgress(shell, ps, getValidOrganization());
                ProgressMonitorDialog progDialog = new AppGetProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "アプリケーション一覧の取得", String.format("TeamServerからエラーが返されました。\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "アプリケーション一覧の取得", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "アプリケーション一覧の取得", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "アプリケーション一覧の取得", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fullAppMap = progress.getFullAppMap();
                if (fullAppMap.isEmpty()) {
                    String userName = ps.getString(PreferenceConstants.USERNAME);
                    StringJoiner sj = new StringJoiner("\r\n");
                    sj.add("アプリケーションの取得件数が０件です。考えられる原因としては以下となります。");
                    sj.add("・下記ユーザーのアプリケーションアクセスグループにView権限が設定されていない。");
                    sj.add(String.format("　%s", userName));
                    sj.add("・Assessライセンスが付与されているアプリケーションがない。");
                    sj.add("・接続設定が正しくない。プロキシの設定がされていない。など");
                    MessageDialog.openInformation(shell, "アプリケーション一覧の取得", sj.toString());
                }
                for (String appLabel : fullAppMap.keySet()) {
                    srcList.add(appLabel); // UI list
                    srcApps.add(appLabel); // memory src
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
            }
        });

        Composite srcGrp = new Composite(appListGrp, SWT.NONE);
        srcGrp.setLayout(new GridLayout(1, false));
        GridData srcGrpGrDt = new GridData(GridData.FILL_BOTH);
        srcGrp.setLayoutData(srcGrpGrDt);

        srcListFilter = new Text(srcGrp, SWT.BORDER);
        srcListFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        srcListFilter.setMessage("Filter...");
        srcListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                srcList.removeAll(); // UI List src
                srcApps.clear(); // memory src
                if (fullAppMap == null) {
                    srcCount.setText(String.valueOf(srcList.getItemCount()));
                    return;
                }
                String keyword = srcListFilter.getText();
                if (keyword.isEmpty()) {
                    for (String appLabel : fullAppMap.keySet()) {
                        if (dstApps.contains(appLabel)) {
                            continue; // 既に選択済みのアプリはスキップ
                        }
                        srcList.add(appLabel); // UI List src
                        srcApps.add(appLabel); // memory src
                    }
                } else {
                    for (String appLabel : fullAppMap.keySet()) {
                        if (appLabel.toLowerCase().contains(keyword.toLowerCase())) {
                            if (dstApps.contains(appLabel)) {
                                continue; // 既に選択済みのアプリはスキップ
                            }
                            srcList.add(appLabel);
                            srcApps.add(appLabel);
                        }
                    }
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                uiUpdateForExclusionButton();
            }
        });
        this.srcList = new org.eclipse.swt.widgets.List(srcGrp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        this.srcList.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.srcList.setToolTipText("選択可能なアプリケーション一覧");
        this.srcList.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int idx = srcList.getSelectionIndex();
                if (idx < 0) {
                    return;
                }
                dstList.add(srcApps.get(idx));
                dstApps.add(srcApps.get(idx));
                srcList.remove(idx);
                srcApps.remove(idx);
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
                uiUpdateForExclusionButton();
            }
        });

        this.srcCount = new Label(srcGrp, SWT.RIGHT);
        GridData srcCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        srcCountGrDt.minimumHeight = 20;
        srcCountGrDt.heightHint = 20;
        this.srcCount.setLayoutData(srcCountGrDt);
        this.srcCount.setFont(new Font(display, "ＭＳ ゴシック", 8, SWT.NORMAL));
        this.srcCount.setText("0");

        Composite btnGrp = new Composite(appListGrp, SWT.NONE);
        btnGrp.setLayout(new GridLayout(1, false));
        GridData btnGrpGrDt = new GridData(GridData.FILL_VERTICAL);
        btnGrpGrDt.verticalAlignment = SWT.CENTER;
        btnGrp.setLayoutData(btnGrpGrDt);

        Button allRightBtn = new Button(btnGrp, SWT.PUSH);
        allRightBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allRightBtn.setText(">>");
        allRightBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (String appName : srcApps) {
                    dstList.add(appName);
                    dstApps.add(appName);
                }
                srcList.removeAll();
                srcApps.clear();
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
                uiUpdateForExclusionButton();
            }
        });

        Button rightBtn = new Button(btnGrp, SWT.PUSH);
        rightBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rightBtn.setText(">");
        rightBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (int idx : srcList.getSelectionIndices()) {
                    String appName = srcApps.get(idx);
                    String keyword = dstListFilter.getText();
                    if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                        dstList.add(appName);
                        dstApps.add(appName);
                    }
                }
                List<Integer> sortedList = Arrays.stream(srcList.getSelectionIndices()).boxed().collect(Collectors.toList());
                Collections.reverse(sortedList);
                for (Integer idx : sortedList) {
                    srcList.remove(idx.intValue());
                    srcApps.remove(idx.intValue());
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
                uiUpdateForExclusionButton();
            }
        });

        Button leftBtn = new Button(btnGrp, SWT.PUSH);
        leftBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        leftBtn.setText("<");
        leftBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (int idx : dstList.getSelectionIndices()) {
                    String appName = dstApps.get(idx);
                    String keyword = srcListFilter.getText();
                    if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                        srcList.add(appName);
                        srcApps.add(appName);
                    }
                }
                List<Integer> sortedList = Arrays.stream(dstList.getSelectionIndices()).boxed().collect(Collectors.toList());
                Collections.reverse(sortedList);
                for (Integer idx : sortedList) {
                    dstList.remove(idx.intValue());
                    dstApps.remove(idx.intValue());
                }
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
                uiUpdateForExclusionButton();
            }
        });

        Button allLeftBtn = new Button(btnGrp, SWT.PUSH);
        allLeftBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        allLeftBtn.setText("<<");
        allLeftBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                for (String appName : dstApps) {
                    srcList.add(appName);
                    srcApps.add(appName);
                }
                dstList.removeAll();
                dstApps.clear();
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
                uiUpdateForExclusionButton();
            }
        });

        Composite dstGrp = new Composite(appListGrp, SWT.NONE);
        dstGrp.setLayout(new GridLayout(1, false));
        GridData dstGrpGrDt = new GridData(GridData.FILL_BOTH);
        dstGrp.setLayoutData(dstGrpGrDt);

        dstListFilter = new Text(dstGrp, SWT.BORDER);
        dstListFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dstListFilter.setMessage("Filter...");
        dstListFilter.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                dstList.removeAll(); // UI List dst
                dstApps.clear(); // memory dst
                if (fullAppMap == null) {
                    dstCount.setText(String.valueOf(dstList.getItemCount()));
                    return;
                }
                String keyword = dstListFilter.getText();
                if (keyword.isEmpty()) {
                    for (String appName : fullAppMap.keySet()) {
                        if (srcApps.contains(appName)) {
                            continue; // 選択可能にあるアプリはスキップ
                        }
                        dstList.add(appName); // UI List dst
                        dstApps.add(appName); // memory dst
                    }
                } else {
                    for (String appName : fullAppMap.keySet()) {
                        if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                            if (srcApps.contains(appName)) {
                                continue; // 選択可能にあるアプリはスキップ
                            }
                            dstList.add(appName); // UI List dst
                            dstApps.add(appName); // memory dst
                        }
                    }
                }
                dstCount.setText(String.valueOf(dstList.getItemCount()));
                uiUpdateForExclusionButton();
            }
        });

        this.dstList = new org.eclipse.swt.widgets.List(dstGrp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        this.dstList.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.dstList.setToolTipText("選択済みのアプリケーション一覧");
        this.dstList.addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int idx = dstList.getSelectionIndex();
                if (idx < 0) {
                    return;
                }
                srcList.add(dstApps.get(idx));
                srcApps.add(dstApps.get(idx));
                dstList.remove(idx);
                dstApps.remove(idx);
                srcCount.setText(String.valueOf(srcList.getItemCount()));
                dstCount.setText(String.valueOf(dstList.getItemCount()));
                uiUpdateForExclusionButton();
            }
        });

        this.dstCount = new Label(dstGrp, SWT.RIGHT);
        GridData dstCountGrDt = new GridData(GridData.FILL_HORIZONTAL);
        dstCountGrDt.minimumHeight = 20;
        dstCountGrDt.heightHint = 20;
        this.dstCount.setLayoutData(dstCountGrDt);
        this.dstCount.setFont(new Font(display, "ＭＳ ゴシック", 8, SWT.NORMAL));
        this.dstCount.setText("0");

        // ========== エクスポートボタン ==========
        exExpBtn = new Button(exBtnGrp, SWT.PUSH);
        GridData exExpBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        exExpBtnGrDt.heightHint = 30;
        exExpBtnGrDt.horizontalSpan = 2;
        exExpBtn.setLayoutData(exExpBtnGrDt);
        exExpBtn.setText("エクスポート");
        exExpBtn.setToolTipText("例外のエクスポート");
        exExpBtn.setFont(new Font(display, "ＭＳ ゴシック", 13, SWT.NORMAL));
        // actionBtns.add(exExpBtn);
        exExpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText("出力先フォルダを指定してください。");
                String dir = dialog.open();
                if (dir == null) {
                    return;
                }
                ExclusionExportWithProgress progress = new ExclusionExportWithProgress(shell, ps, dstApps, fullAppMap, dir);
                ProgressMonitorDialog progDialog = new ExclusionExportProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "例外のエクスポート", String.format("TeamServerからエラーが返されました。\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "例外のエクスポート", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "例外のエクスポート", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "例外のエクスポート", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // ========== 削除ボタン ==========
        Group exDelGrp = new Group(exBtnGrp, SWT.NULL);
        GridLayout exDelGrpLt = new GridLayout(2, false);
        exDelGrpLt.marginWidth = 2;
        exDelGrpLt.marginHeight = 0;
        exDelGrp.setLayout(exDelGrpLt);
        GridData exDelGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        exDelGrpGrDt.horizontalSpan = 2;
        // deleteExGrpGrDt.heightHint = 140;
        exDelGrp.setLayoutData(exDelGrpGrDt);

        exDelBtn = new Button(exDelGrp, SWT.PUSH);
        GridData exDelBtnGrDt = new GridData(GridData.FILL_BOTH);
        exDelBtn.setLayoutData(exDelBtnGrDt);
        exDelBtn.setText("削除対象を表示");
        exDelBtn.setToolTipText("例外の削除");
        exDelBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        // actionBtns.add(exDelBtn);
        exDelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.size() != 1) {
                    return;
                }
                AppInfo appInfo = fullAppMap.get(dstApps.get(0));
                String filterWord = exFilterWordTxt.getText().trim();
                ExclusionDeleteWithProgress progress = new ExclusionDeleteWithProgress(shell, ps, appInfo, filterWord);
                ProgressMonitorDialog progDialog = new ExclusionDeleteProgressMonitorDialog(shell, appInfo);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "例外のエクスポート", String.format("TeamServerからエラーが返されました。\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "例外のエクスポート", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "例外のエクスポート", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "例外のエクスポート", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        exFilterWordTxt = new Text(exDelGrp, SWT.BORDER);
        exFilterWordTxt.setText(ps.getString(PreferenceConstants.EXCLUSION_DEL_FILTER_WORD));
        exFilterWordTxt.setMessage("例) hoge, foo_*, *bar*, *_baz");
        exFilterWordTxt.setToolTipText("削除対象を指定します。アスタリスク使用で前方、後方、部分一致を指定できます。カンマ区切りで複数指定可能です。");
        exFilterWordTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        exFilterWordTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                exFilterWordTxt.selectAll();
            }
        });

        // ========== インポートボタン ==========
        Group exImpGrp = new Group(exBtnGrp, SWT.NULL);
        GridLayout exImpGrpLt = new GridLayout(4, false);
        exImpGrpLt.marginWidth = 2;
        exImpGrpLt.marginHeight = 0;
        exImpGrp.setLayout(exImpGrpLt);
        GridData exImpGrpGrDt = new GridData(GridData.FILL_BOTH);
        exImpGrpGrDt.horizontalSpan = 2;
        exImpGrp.setLayoutData(exImpGrpGrDt);

        new Label(exImpGrp, SWT.NONE).setText("URLのパス置換: ");

        exImpRepBefWordTxt = new Text(exImpGrp, SWT.BORDER);
        exImpRepBefWordTxt.setText(ps.getString(PreferenceConstants.EXCLUSION_IMP_REP_BEF_WORD));
        exImpRepBefWordTxt.setMessage("例) /CONTEXT_PATH");
        exImpRepBefWordTxt.setToolTipText("urlsの値を置換する際の置換前のパスを指定してください。");
        exImpRepBefWordTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        exImpRepBefWordTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                exImpRepBefWordTxt.selectAll();
            }
        });

        new Label(exImpGrp, SWT.NONE).setText("→");

        exImpRepAftWordTxt = new Text(exImpGrp, SWT.BORDER);
        exImpRepAftWordTxt.setText(ps.getString(PreferenceConstants.EXCLUSION_IMP_REP_AFT_WORD));
        exImpRepAftWordTxt.setMessage("例) /imart");
        exImpRepAftWordTxt.setToolTipText("urlsの値を置換する際の置換後のパスを指定してください。");
        exImpRepAftWordTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        exImpRepAftWordTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                exImpRepAftWordTxt.selectAll();
            }
        });

        exImpBtn = new Button(exImpGrp, SWT.PUSH);
        GridData exImpBtnGrDt = new GridData(GridData.FILL_BOTH);
        exImpBtnGrDt.heightHint = 50;
        exImpBtnGrDt.horizontalSpan = 4;
        exImpBtn.setLayoutData(exImpBtnGrDt);
        exImpBtn.setText("インポート");
        exImpBtn.setToolTipText("例外のインポート");
        exImpBtn.setFont(new Font(display, "ＭＳ ゴシック", 18, SWT.NORMAL));
        // actionBtns.add(exImpBtn);
        exImpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.size() != 1) {
                    return;
                }
                String exImpRepBefWord = exImpRepBefWordTxt.getText().trim();
                String exImpRepAftWord = exImpRepAftWordTxt.getText().trim();
                if ((!exImpRepBefWord.isEmpty() && exImpRepAftWord.isEmpty()) || (exImpRepBefWord.isEmpty() && !exImpRepAftWord.isEmpty())) {
                    MessageDialog.openError(shell, "例外のインポート", "URLのパス置換を設定する場合は両方とも設定してください。");
                    return;
                }
                AppInfo appInfo = fullAppMap.get(dstApps.get(0));
                FileDialog dialog = new FileDialog(shell);
                dialog.setText("インポートするjsonファイルを指定してください。");
                dialog.setFilterExtensions(new String[] { "*.json" });
                String file = dialog.open();
                if (file == null) {
                    return;
                }
                ExclusionImportWithProgress progress = new ExclusionImportWithProgress(shell, ps, appInfo, exImpRepBefWord, exImpRepAftWord, file);
                ProgressMonitorDialog progDialog = new ExclusionImportProgressMonitorDialog(shell, appInfo);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // ========== 差分確認ボタン ==========
        exCmpBtn = new Button(exImpGrp, SWT.PUSH);
        GridData exCmpBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // exceptionCompareBtnGrDt.heightHint = 30;
        exCmpBtnGrDt.horizontalSpan = 4;
        exCmpBtn.setLayoutData(exCmpBtnGrDt);
        exCmpBtn.setText("インポート済みチェック");
        exCmpBtn.setToolTipText("例外が正しくインポートされているかを確認します。");
        exCmpBtn.setFont(new Font(display, "ＭＳ ゴシック", 13, SWT.NORMAL));
        // actionBtns.add(exCmpBtn);
        exCmpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (dstApps.size() != 1) {
                    return;
                }
                String exImpRepBefWord = exImpRepBefWordTxt.getText().trim();
                String exImpRepAftWord = exImpRepAftWordTxt.getText().trim();
                if ((!exImpRepBefWord.isEmpty() && exImpRepAftWord.isEmpty()) || (exImpRepBefWord.isEmpty() && !exImpRepAftWord.isEmpty())) {
                    MessageDialog.openError(shell, "例外のインポート", "URLのパス置換を設定する場合は両方とも設定してください。");
                    return;
                }
                AppInfo appInfo = fullAppMap.get(dstApps.get(0));
                FileDialog dialog = new FileDialog(shell);
                dialog.setText("比較する対象のjsonファイルを指定してください。");
                dialog.setFilterExtensions(new String[] { "*.json" });
                String file = dialog.open();
                if (file == null) {
                    return;
                }
                ExclusionCompareWithProgress progress = new ExclusionCompareWithProgress(shell, ps, appInfo, exImpRepBefWord, exImpRepAftWord, file);
                ProgressMonitorDialog progDialog = new ExclusionCompareProgressMonitorDialog(shell, appInfo);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // ========== スケルトン生成ボタン ==========
        exSklBtn = new Button(exBtnGrp, SWT.PUSH);
        GridData exSklBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        exSklBtn.setLayoutData(exSklBtnGrDt);
        exSklBtn.setText("スケルトンJSON出力");
        exSklBtn.setToolTipText("例外のインポートJSONファイルのスケルトン生成");
        exSklBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        exSklBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText("出力先フォルダを指定してください。");
                String dir = dialog.open();
                if (dir == null) {
                    return;
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try {
                    String fileName = dir + "\\exclusion_skeleton.json";
                    Writer writer = new FileWriter(fileName);
                    List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
                    Map<String, Object> map1 = new HashMap<String, Object>();
                    map1.put("name", "Code_foo");
                    map1.put("type", "CODE");
                    map1.put("codes", Arrays.asList(new String[] { "jp.co.contrast.foo", "jp.co.contrast.bar" }));
                    map1.put("all_rules", true);
                    map1.put("all_assessment_rules", false);
                    map1.put("all_protection_rules", false);
                    map1.put("assessment_rules", Arrays.asList(new String[0]));
                    map1.put("protection_rules", Arrays.asList(new String[0]));
                    map1.put("input_name", "");
                    map1.put("input_type", "");
                    mapList.add(map1);

                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("name", "Input_bar");
                    map2.put("type", "INPUT");
                    map2.put("urls", Arrays.asList(new String[] { "/test1", "/test2" }));
                    map2.put("all_rules", true);
                    map2.put("all_assessment_rules", false);
                    map2.put("all_protection_rules", false);
                    map2.put("assessment_rules", Arrays.asList(new String[0]));
                    map2.put("protection_rules", Arrays.asList(new String[0]));
                    map2.put("input_name", "");
                    map2.put("input_type", "");
                    map2.put("url_pattern_type", "ONLY");
                    mapList.add(map2);

                    Map<String, Object> map3 = new HashMap<String, Object>();
                    map3.put("name", "URL_baz");
                    map3.put("type", "URL");
                    map3.put("urls", Arrays.asList(new String[] { "crypto-bad-ciphers" }));
                    map3.put("all_rules", false);
                    map3.put("all_assessment_rules", false);
                    map3.put("all_protection_rules", false);
                    map3.put("assessment_rules", Arrays.asList(new String[0]));
                    map3.put("protection_rules", Arrays.asList(new String[0]));
                    map3.put("input_name", "");
                    map3.put("input_type", "");
                    map3.put("url_pattern_type", "ONLY");

                    mapList.add(map3);
                    gson.toJson(mapList, writer);
                    writer.close();
                    MessageDialog.openInformation(shell, "例外のスケルトンJSON出力", String.format("スケルトンJSONファイルを出力しました。\r\n%s", fileName));
                } catch (Exception e) {
                    MessageDialog.openError(shell, "例外のスケルトンJSON出力", e.getMessage());
                }
            }
        });

        exRulesShowBtn = new Button(exBtnGrp, SWT.PUSH);
        exRulesShowBtn.setText("ルール一覧");
        exRulesShowBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        // actionBtns.add(exRulesShowBtn);
        exRulesShowBtn.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (dstApps.size() != 1) {
                    return;
                }
                AppInfo appInfo = fullAppMap.get(dstApps.get(0));
                Api configsApi = new AssessRulesConfigurationsApi(shell, ps, getValidOrganization(), appInfo.getAppId());
                Api policiesApi = new ProtectionPoliciesApi(shell, ps, getValidOrganization(), appInfo.getAppId());
                try {
                    List<AssessRulesConfiguration> configs = (List<AssessRulesConfiguration>) configsApi.get();
                    List<ProtectionPolicy> policies = null;
                    try {
                        policies = (List<ProtectionPolicy>) policiesApi.get();
                    } catch (ApiException ae) {
                        if (ae.getResponse_code() == 403) {
                            policies = new ArrayList<ProtectionPolicy>();
                        } else {
                            throw ae;
                        }
                    }
                    ExclusionRulesShowDialog rulesShowDialog = new ExclusionRulesShowDialog(shell, getValidOrganization(), appInfo, configs, policies);
                    rulesShowDialog.open();
                } catch (Exception e2) {
                    MessageDialog.openError(shell, "ルール一覧", String.format("エラーが発生しました。ログファイルをご確認ください。\r\n%s", e2.getMessage()));
                }
            }
        });

        exTabItem.setControl(exShell);

        int main_idx = this.ps.getInt(PreferenceConstants.OPENED_MAIN_TAB_IDX);
        mainTabFolder.setSelection(main_idx);

        // ========== 設定ボタン ==========
        settingBtn = new Button(shell, SWT.PUSH);
        settingBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        settingBtn.setText("設定");
        settingBtn.setToolTipText("動作に必要な設定を行います。");
        settingBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                PreferenceManager mgr = new PreferenceManager();
                PreferenceNode baseNode = new PreferenceNode("base", new BasePreferencePage());
                PreferenceNode connectionNode = new PreferenceNode("connection", new ConnectionPreferencePage());
                PreferenceNode otherNode = new PreferenceNode("other", new OtherPreferencePage());
                mgr.addToRoot(baseNode);
                mgr.addToRoot(connectionNode);
                mgr.addToRoot(otherNode);
                PreferenceNode aboutNode = new PreferenceNode("about", new AboutPage());
                mgr.addToRoot(aboutNode);
                PreferenceDialog dialog = new MyPreferenceDialog(shell, mgr);
                dialog.setPreferenceStore(ps);
                dialog.open();
                try {
                    ps.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        uiUpdate();
        int width = this.ps.getInt(PreferenceConstants.MEM_WIDTH);
        int height = this.ps.getInt(PreferenceConstants.MEM_HEIGHT);
        if (width > 0 && height > 0) {
            shell.setSize(width, height);
        } else {
            shell.setSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
            // shell.pack();
        }
        shell.open();
        try {
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String trace = stringWriter.toString();
            logger.error(trace);
        }
        display.dispose();
    }

    private void uiReset() {
        // src
        srcListFilter.setText("");
        srcList.removeAll();
        srcApps.clear();
        // dst
        dstListFilter.setText("");
        dstList.removeAll();
        dstApps.clear();
        // full
        if (fullAppMap != null) {
            fullAppMap.clear();
        }
    }

    private void uiUpdate() {
    }

    private void uiUpdateForExclusionButton() {
        if (dstList.getItemCount() == 0) {
            exExpBtn.setEnabled(false);
            exDelBtn.setEnabled(false);
            exImpBtn.setEnabled(false);
            exCmpBtn.setEnabled(false);
            exRulesShowBtn.setEnabled(false);
        } else if (dstList.getItemCount() == 1) {
            exExpBtn.setEnabled(true);
            exDelBtn.setEnabled(true);
            exImpBtn.setEnabled(true);
            exCmpBtn.setEnabled(true);
            exRulesShowBtn.setEnabled(true);
        } else {
            exExpBtn.setEnabled(true);
            exDelBtn.setEnabled(false);
            exImpBtn.setEnabled(false);
            exCmpBtn.setEnabled(false);
            exRulesShowBtn.setEnabled(false);
        }
    }

    public PreferenceStore getPreferenceStore() {
        return ps;
    }

    public Organization getValidOrganization() {
        String orgJsonStr = ps.getString(PreferenceConstants.TARGET_ORGS);
        if (orgJsonStr.trim().length() > 0) {
            try {
                List<Organization> orgList = new Gson().fromJson(orgJsonStr, new TypeToken<List<Organization>>() {
                }.getType());
                for (Organization org : orgList) {
                    if (org != null && org.isValid()) {
                        return org;
                    }
                }
            } catch (JsonSyntaxException e) {
                return null;
            }
        }
        return null;
    }

    public void setWindowTitle() {
        String text = null;
        Organization validOrg = getValidOrganization();
        if (validOrg != null) {
            text = validOrg.getName();
        }
        if (text == null || text.isEmpty()) {
            this.shell.setText(String.format(WINDOW_TITLE, "組織未設定"));
        } else {
            this.shell.setText(String.format(WINDOW_TITLE, text));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("tsv".equals(event.getPropertyName())) {
            System.out.println("tsv main");
        }
    }

    /**
     * @param listener
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    /**
     * @param listener
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }
}
