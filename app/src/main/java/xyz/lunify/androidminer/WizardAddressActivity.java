// Copyright (c) 2025 Lunify
//
// Please see the included LICENSE file for more information.

package xyz.lunify.androidminer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.integration.android.IntentIntegrator;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import xyz.lunify.androidminer.widgets.Notice;
import xyz.lunify.androidminer.widgets.Toolbar;

public class WizardAddressActivity extends BaseActivity {
    private TextView tvAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setContentView(R.layout.fragment_wizard_address);
        View view2 = findViewById(android.R.id.content).getRootView();
        tvAddress = view2.findViewById(R.id.addressWizard);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        toolbar.setOnButtonListener(new Toolbar.OnButtonListener() {
            @Override
            public void onButtonMain(int type) {
                if (type == Toolbar.BUTTON_MAIN_BACK) {
                    finish();
                }
            }

            @Override
            public void onButtonOptions(int type) {
                onMineLunify();
            }
        });

        toolbar.setTitle("Wallet Address");
        toolbar.setButtonMain(Toolbar.BUTTON_MAIN_BACK);
        toolbar.setButtonOptions(Toolbar.BUTTON_OPTIONS_STAR);

        Button btnWalletAddressHelp = findViewById(R.id.btnWalletAddressHelp);
        btnWalletAddressHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showPopup(WizardAddressActivity.this, getString(R.string.walletaddress2), getString(R.string.mining_address_help));
            }
        });

        ViewGroup llNotice = findViewById(R.id.llNotice);
        llNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WizardAddressActivity.this, VaultActivity.class));
            }
        });
        Notice.showAll(llNotice, Notice.NOTICE_SHOW_VAULT, false);
    }

    public void onPaste(View view) {
        tvAddress.setText(Utils.pasteFromClipboard(WizardAddressActivity.this));
        Utils.hideKeyboard(this);
    }

    public void onScanQrCode(View view) {
        Context appContext = WizardAddressActivity.this;

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            } else {
                startQrCodeActivity();
            }
        } else if (Build.VERSION.SDK_INT >= 21) {
            startQrCodeActivity();
        } else {
            Utils.showToast(appContext, "This version of Android does not support Qr Code.", Toast.LENGTH_LONG);
        }
    }

    private void startQrCodeActivity() {
        Context appContext = WizardAddressActivity.this;

        try {
            new IntentIntegrator(this).setOrientationLocked(false).setCaptureActivity(QrCodeScannerActivity.class).initiateScan();
        } catch (Exception e) {
            Utils.showToast(appContext, e.getMessage(), Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        Context appContext = WizardAddressActivity.this;

        if (requestCode == 100) {
            if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQrCodeActivity();
            } else {
                Utils.showToast(appContext, "Camera Permission Denied.", Toast.LENGTH_LONG);
            }
        }
    }

    public void onNext(View view) {
        String strAddress = tvAddress.getText().toString().trim();

        View view2 = findViewById(android.R.id.content).getRootView();
        TextInputLayout til = view2.findViewById(R.id.addressIL);

        if (strAddress.isEmpty() || !Utils.verifyAddress(strAddress)) {
            til.setErrorEnabled(true);
            til.setError(getResources().getString(R.string.invalidaddress));
            requestFocus(tvAddress);
            return;
        }

        til.setErrorEnabled(false);
        til.setError(null);

        Config.write(Config.CONFIG_ADDRESS, strAddress);

        Intent intent = new Intent(WizardAddressActivity.this, PoolActivity.class);
        intent.putExtra(PoolActivity.RequesterType, PoolActivity.REQUESTER_WIZARD);
        startActivity(intent);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvAddress.setText(Config.read(Config.CONFIG_ADDRESS));

    }

    public void onMineLunify() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(WizardAddressActivity.this, R.style.MaterialAlertDialogCustom);
        builder.setTitle(getString(R.string.supporttheproject))
                .setMessage(getString(R.string.minetolunify))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        View view2 = findViewById(android.R.id.content).getRootView();
                        TextView tvAddress = view2.findViewById(R.id.addressWizard);
                        tvAddress.setText(Utils.LUNIFY_LFI_ADDRESS);
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}