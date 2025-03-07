// Copyright (c) 2019, Mine2Gether.com
//
// Please see the included LICENSE file for more information.
//
// Copyright (c) 2025 Lunify
//
// Please see the included LICENSE file for more information.

package xyz.lunify.androidminer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.integration.android.IntentIntegrator;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import xyz.lunify.androidminer.api.PoolItem;
import xyz.lunify.androidminer.api.ProviderManager;
import xyz.lunify.androidminer.widgets.Notice;
import xyz.lunify.androidminer.widgets.PoolView;

public class SettingsFragment extends Fragment {

    private static final String LOG_TAG = "MiningSvc";

    private TextInputLayout tilAddress;
    private EditText edAddress, edWorkerName, edUsernameparameters;

    private PoolView pvSelectedPool;

    public static PoolItem selectedPoolTmp = null;

    private boolean bDefineBatteryLevel = false;
    private Integer nBatteryLevel = Config.DefaultBatteryLevel;

    private Integer nMaxCPUTemp = Config.DefaultMaxCPUTemp; // 60,65,70,75,80
    private Integer nMaxBatteryTemp = Config.DefaultMaxBatteryTemp; // 30,35,40,45,50
    private Integer nCooldownTheshold = Config.DefaultCooldownTheshold; // 5,10,15,20,25

    private SeekBar sbCPUTemp, sbBatteryTemp, sbCooldown, sbCores;
    private TextView tvCPUMaxTemp, tvBatteryMaxTemp, tvCooldown, tvCPUTempUnit, tvBatteryTempUnit, tvRefreshHashrateDelay;
    private Switch swDisableTempControl, swPauseOnBattery, swPauseOnNetwork, swKeepScreenOnWhenMining, swSendDebugInformation;

    private ImageView ivDecreaseRefreshHashrateDelay, ivIncreaseRefreshHashrateDelay;
    private MaterialButtonToggleGroup tgTemperatureUnit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ProviderManager.getPools(getContext());

        Button bSave;

        TextView tvCoresNb, tvCoresMax;

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        bSave = view.findViewById(R.id.saveSettings);

        ViewGroup llNotice = view.findViewById(R.id.llNotice);
        llNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), VaultActivity.class));
            }
        });
        Notice.showAll(llNotice, Notice.NOTICE_SHOW_VAULT, false);

        tilAddress = view.findViewById(R.id.addressIL);
        edAddress = view.findViewById(R.id.address);

        pvSelectedPool = view.findViewById(R.id.viewPool);
        pvSelectedPool.setOnButtonListener(new PoolView.OnButtonListener() {
            @Override
            public void onButton() {
                onOpenPools();
            }
        });

        pvSelectedPool.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                onOpenPools();
            }
        });

        edUsernameparameters = view.findViewById(R.id.usernameparameters);
        edWorkerName = view.findViewById(R.id.workername);

        Button bQrCode = view.findViewById(R.id.btnQrCode);

        sbCores = view.findViewById(R.id.seekbarcores);
        tvCoresNb = view.findViewById(R.id.coresnb);
        tvCoresMax = view.findViewById(R.id.coresmax);

        sbCPUTemp = view.findViewById(R.id.seekbarcputemperature);
        tvCPUMaxTemp = view.findViewById(R.id.cpumaxtemp);
        tvCPUTempUnit = view.findViewById(R.id.cputempunit);

        sbBatteryTemp = view.findViewById(R.id.seekbarbatterytemperature);
        tvBatteryMaxTemp = view.findViewById(R.id.batterymaxtemp);
        tvBatteryTempUnit = view.findViewById(R.id.batterytempunit);

        sbCooldown = view.findViewById(R.id.seekbarcooldownthreshold);
        tvCooldown = view.findViewById(R.id.cooldownthreshold);

        tvRefreshHashrateDelay = view.findViewById(R.id.tvRefreshHashrateDelay);

        swPauseOnBattery = view.findViewById(R.id.chkPauseOnBattery);
        swPauseOnNetwork = view.findViewById(R.id.chkPauseOnNetwork);
        swKeepScreenOnWhenMining = view.findViewById(R.id.chkKeepScreenOnWhenMining);
        swDisableTempControl = view.findViewById(R.id.chkAmaycOff);
        swSendDebugInformation = view.findViewById(R.id.chkSendDebugInformation);

        bDefineBatteryLevel = Config.read(Config.CONFIG_BATTERY_LEVEL_ENABLED, "0").equals("1");
        nBatteryLevel = Integer.valueOf(Config.read(Config.CONFIG_BATTERY_LEVEL, Integer.toString(Config.DefaultBatteryLevel)));

        ImageView ivBatteryLevelOptions = view.findViewById(R.id.ivBatteryLevelOptions);
        ivBatteryLevelOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseMiningBattery(v);
            }
        });

        swPauseOnBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((Switch)v).isChecked();
                if(checked)
                    onPauseMiningBattery(v);
            }
        });

        tgTemperatureUnit = view.findViewById(R.id.tgTemperatureUnit);
        tgTemperatureUnit.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if(checkedId == R.id.btnFarehnheit) {
                    tvCPUTempUnit.setText(getString(R.string.celsius));
                    tvBatteryTempUnit.setText(getString(R.string.celsius));
                } else {
                    tvCPUTempUnit.setText(getString(R.string.fahrenheit));
                    tvBatteryTempUnit.setText(getString(R.string.fahrenheit));
                }

                updateCPUTemp();
                updateBatteryTemp();
            }
        });

        // CPU Cores
        int cores = Runtime.getRuntime().availableProcessors();

        int suggested = cores / 2;
        if (suggested == 0) suggested = 1;

        sbCores.setMax(cores-1);
        tvCoresMax.setText(Integer.toString(cores));

        if (Config.read(Config.CONFIG_CORES).isEmpty()) {
            sbCores.setProgress(suggested-1);
            tvCoresNb.setText(Integer.toString(suggested));
        } else {
            int corenb = Integer.parseInt(Config.read(Config.CONFIG_CORES));
            sbCores.setProgress(corenb-1);
            tvCoresNb.setText(Integer.toString(corenb));
        }

        String temperature_unit = Config.read(Config.CONFIG_TEMPERATURE_UNIT, "C");
        tgTemperatureUnit.check(temperature_unit.equals("C") ? R.id.btnCelsius : R.id.btnFarehnheit);

        if(temperature_unit.equals("C")) {
            tvCPUTempUnit.setText(getString(R.string.celsius));
            tvBatteryTempUnit.setText(getString(R.string.celsius));
        } else {
            tvCPUTempUnit.setText(getString(R.string.fahrenheit));
            tvBatteryTempUnit.setText(getString(R.string.fahrenheit));
        }

        if (!Config.read(Config.CONFIG_MAX_CPU_TEMP).isEmpty()) {
            nMaxCPUTemp = Integer.parseInt(Config.read(Config.CONFIG_MAX_CPU_TEMP));
        }
        int nProgress = ((nMaxCPUTemp-Utils.MIN_CPU_TEMP)/Utils.INCREMENT);
        sbCPUTemp.setProgress(nProgress);
        updateCPUTemp();

        if (!Config.read(Config.CONFIG_MAX_BATTERY_TEMP).isEmpty()) {
            nMaxBatteryTemp = Integer.parseInt(Config.read(Config.CONFIG_MAX_BATTERY_TEMP));
        }
        nProgress = ((nMaxBatteryTemp-Utils.MIN_BATTERY_TEMP)/Utils.INCREMENT);
        sbBatteryTemp.setProgress(nProgress);
        updateBatteryTemp();

        if (!Config.read(Config.CONFIG_COOLDOWN_THRESHOLD).isEmpty()) {
            nCooldownTheshold = Integer.parseInt(Config.read(Config.CONFIG_COOLDOWN_THRESHOLD));
        }
        nProgress = ((nCooldownTheshold-Utils.MIN_COOLDOWN)/Utils.INCREMENT);
        sbCooldown.setProgress(nProgress);
        updateCooldownThreshold();

        boolean disableTempControl = (Config.read(Config.CONFIG_DISABLE_TEMPERATURE_CONTROL).equals("1"));
        if(disableTempControl){
            swDisableTempControl.setChecked(true);
        }
        enableTemperatureControl(!disableTempControl);

        boolean checkPauseOnBattery = Config.read(Config.CONFIG_PAUSE_ON_BATTERY).equals("1");
        if(checkPauseOnBattery) {
            swPauseOnBattery.setChecked(true);
        }

        boolean checkPauseOnNetwork = Config.read(Config.CONFIG_PAUSE_ON_NETWORK).equals("1");
        if(checkPauseOnNetwork) {
            swPauseOnNetwork.setChecked(true);
        }

        boolean checkStatusScreenOn = Config.read(Config.CONFIG_KEEP_SCREEN_ON_WHEN_MINING).equals("1");
        if(checkStatusScreenOn) {
            swKeepScreenOnWhenMining.setChecked(true);
        }

        boolean checkSendDebugInformation = Config.read(Config.CONFIG_SEND_DEBUG_INFO).equals("1");
        if(checkSendDebugInformation) {
            swSendDebugInformation.setChecked(true);
        }

        if (!Config.read(Config.CONFIG_ADDRESS).isEmpty()) {
            edAddress.setText(Config.read(Config.CONFIG_ADDRESS));
        }

        if (!Config.read(Config.CONFIG_USERNAME_PARAMETERS).isEmpty()) {
            edUsernameparameters.setText(Config.read(Config.CONFIG_USERNAME_PARAMETERS));
        }

        if (!Config.read(Config.CONFIG_WORKERNAME).isEmpty()) {
            edWorkerName.setText(Config.read(Config.CONFIG_WORKERNAME));
        }

        if (!Config.read(Config.CONFIG_HASHRATE_REFRESH_DELAY).isEmpty()) {
            tvRefreshHashrateDelay.setText(Config.read(Config.CONFIG_HASHRATE_REFRESH_DELAY));
        } else {
            tvRefreshHashrateDelay.setText(Integer.toString(Config.DefaultRefreshDelay));
        }

        sbCores.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvCoresNb.setText(Integer.toString(progress+1));
            }
        });

        sbCPUTemp.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateCPUTemp();
            }
        });

        sbBatteryTemp.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateBatteryTemp();
            }
        });

        sbCooldown.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateCooldownThreshold();
            }
        });

        ivDecreaseRefreshHashrateDelay = view.findViewById(R.id.ivDecreaseRefreshHashrateDelay);
        ivDecreaseRefreshHashrateDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDecreaseHashrateRefreshDelay();
            }
        });

        ivIncreaseRefreshHashrateDelay = view.findViewById(R.id.ivIncreaseRefreshHashrateDelay);
        ivIncreaseRefreshHashrateDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onIncreaseHashrateRefreshDelay();
            }
        });

        updateHashrateRefreshDelayControls();

        swDisableTempControl.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                boolean checked = ((Switch)v).isChecked();
                if (checked) {
                    ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.c_yellow));

                    String title = getString(R.string.warning);
                    SpannableStringBuilder ssBuilder = new SpannableStringBuilder(title);
                    ssBuilder.setSpan(
                            foregroundColorSpan,
                            0,
                            title.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );

                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()), R.style.MaterialAlertDialogCustom);
                    builder.setTitle(ssBuilder)
                            .setMessage(Html.fromHtml(getString(R.string.warning_temperature_control_prompt)))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.yes), null)
                            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    swDisableTempControl.setChecked(false);
                                }
                            })
                            .show();
                }

                enableTemperatureControl(!checked);
            }
        });

        selectedPoolTmp = null;

        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if mining, ask to restart
                if(MainActivity.isDeviceMiningBackground()) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Objects.requireNonNull(getContext()), R.style.MaterialAlertDialogCustom);
                    builder.setTitle(getString(R.string.stopmining))
                            .setMessage(getString(R.string.newparametersapplied))
                            .setCancelable(true)
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    saveSettings();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Do nothing
                                }
                            })
                            .show();
                } else {
                    saveSettings();
                }
            }
        });

        Button btnPasteAddress = view.findViewById(R.id.btnPasteAddress);
        btnPasteAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edAddress.setText(Utils.pasteFromClipboard(MainActivity.getContextOfApplication()));
                Utils.hideKeyboard(getActivity());
            }
        });

        bQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context appContext = MainActivity.getContextOfApplication();
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                    }
                    else {
                        startQrCodeActivity();
                    }
                }
                else {
                    Utils.showToast(appContext, "This version of Android does not support QR Code.", Toast.LENGTH_LONG);
                }
            }
        });

        Button btnMineLunify = view.findViewById(R.id.btnMineLunify);
        btnMineLunify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Objects.requireNonNull(getContext()), R.style.MaterialAlertDialogCustom);
                builder.setTitle(getString(R.string.supporttheproject))
                        .setMessage(getString(R.string.minetolunify))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                edAddress.setText(Utils.LUNIFY_LFI_ADDRESS);
                            }
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
            }
        });

        Button btnAddressHelp = view.findViewById(R.id.btnAddressHelp);
        btnAddressHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showPopup(getContext(), getString(R.string.walletaddress2), getString(R.string.mining_address_help));
            }
        });

        Button btnTemperatureControlHelp = view.findViewById(R.id.btnTemperatureControlHelp);
        btnTemperatureControlHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showPopup(getContext(), getString(R.string.temperature_control), getString(R.string.hardware_settings_help));
            }
        });

        Button btnAmaycWarning = view.findViewById(R.id.btnAmaycWarning);
        btnAmaycWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showPopup(getContext(), getString(R.string.temperature_control), Html.fromHtml(getString(R.string.warning_temperature_control)).toString());
            }
        });

        Button btnSendDebugInformationHelp = view.findViewById(R.id.btnSendDebugInformationHelp);
        btnSendDebugInformationHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showPopup(getContext(), getResources().getString(R.string.send_debug_information), getResources().getString(R.string.send_debug_information_help));
            }
        });

        return view;
    }

    private void onPauseMiningBattery(View view) {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialogCustom);
        LayoutInflater li = LayoutInflater.from(alertDialogBuilder.getContext());
        View promptsView = li.inflate(R.layout.prompt_battery_level, null);
        alertDialogBuilder.setView(promptsView);

        TextView tvBatteryLevel = promptsView.findViewById(R.id.tvBatteryLevel);
        ImageView ivDecreaseBatteryLevel = promptsView.findViewById(R.id.ivDecreaseBatteryLevel);
        ImageView ivIncreaseBatteryLevel = promptsView.findViewById(R.id.ivIncreaseBatteryLevel);

        Switch swBatteryLevel = promptsView.findViewById(R.id.chkBatteryLevel);
        swBatteryLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((Switch)v).isChecked();
                int level = Integer.parseInt(tvBatteryLevel.getText().toString());
                boolean enabled = level > 5 && checked;
                ivDecreaseBatteryLevel.setEnabled(enabled);
                ivDecreaseBatteryLevel.setColorFilter(enabled ? getResources().getColor(R.color.c_purple) : getResources().getColor(R.color.txt_inactive));

                enabled = level < 95 && checked;
                ivIncreaseBatteryLevel.setEnabled(enabled);
                ivIncreaseBatteryLevel.setColorFilter(enabled ? getResources().getColor(R.color.c_purple) : getResources().getColor(R.color.txt_inactive));
            }
        });

        // Load initial values
        swBatteryLevel.setChecked(bDefineBatteryLevel);
        tvBatteryLevel.setText(String.valueOf(nBatteryLevel));

        int level = Integer.parseInt(tvBatteryLevel.getText().toString());
        boolean enabled = level > 5 && swBatteryLevel.isChecked();
        ivDecreaseBatteryLevel.setEnabled(enabled);
        ivDecreaseBatteryLevel.setColorFilter(enabled ? getResources().getColor(R.color.c_purple) : getResources().getColor(R.color.txt_inactive));

        enabled = level < 95 && swBatteryLevel.isChecked();
        ivIncreaseBatteryLevel.setEnabled(enabled);
        ivIncreaseBatteryLevel.setColorFilter(enabled ? getResources().getColor(R.color.c_purple) : getResources().getColor(R.color.txt_inactive));

        ivDecreaseBatteryLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int level = Integer.parseInt(tvBatteryLevel.getText().toString());
                if(level > 5)
                    tvBatteryLevel.setText(Integer.toString(level-5));

                level = Integer.parseInt(tvBatteryLevel.getText().toString());
                ivDecreaseBatteryLevel.setEnabled(level > 5);
                ivDecreaseBatteryLevel.setColorFilter(level > 5 ? getResources().getColor(R.color.c_purple) : getResources().getColor(R.color.txt_inactive));

                ivIncreaseBatteryLevel.setEnabled(level < 95);
                ivIncreaseBatteryLevel.setColorFilter(level < 95 ? getResources().getColor(R.color.c_purple) : getResources().getColor(R.color.txt_inactive));
            }
        });

        ivIncreaseBatteryLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int level = Integer.parseInt(tvBatteryLevel.getText().toString());
                if(level < 95)
                    tvBatteryLevel.setText(Integer.toString(level+5));

                level = Integer.parseInt(tvBatteryLevel.getText().toString());
                ivDecreaseBatteryLevel.setEnabled(level > 5);
                ivDecreaseBatteryLevel.setColorFilter(level > 5 ? getResources().getColor(R.color.c_purple) : getResources().getColor(R.color.txt_inactive));

                ivIncreaseBatteryLevel.setEnabled(level < 95);
                ivIncreaseBatteryLevel.setColorFilter(level < 95 ? getResources().getColor(R.color.c_purple) : getResources().getColor(R.color.txt_inactive));
            }
        });

        // set dialog message
        alertDialogBuilder
                .setTitle(getResources().getString(R.string.battery_level))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        bDefineBatteryLevel = swBatteryLevel.isChecked();
                        nBatteryLevel = Integer.valueOf(tvBatteryLevel.getText().toString());
                    }
                });

        alertDialogBuilder.show();
    }

    private void updateHashrateRefreshDelayControls() {
        int delay = Integer.parseInt(tvRefreshHashrateDelay.getText().toString());
        ivDecreaseRefreshHashrateDelay.setEnabled(delay > 1);
        ivDecreaseRefreshHashrateDelay.setColorFilter(delay > 1 ? getResources().getColor(R.color.c_purple) : getResources().getColor(R.color.txt_inactive));

        ivIncreaseRefreshHashrateDelay.setEnabled(delay < Config.DefaultRefreshDelay);
        ivIncreaseRefreshHashrateDelay.setColorFilter(delay < Config.DefaultRefreshDelay ? getResources().getColor(R.color.c_purple) : getResources().getColor(R.color.txt_inactive));
    }

    public void onDecreaseHashrateRefreshDelay() {
        int delay = Integer.parseInt(tvRefreshHashrateDelay.getText().toString());
        if(delay > 1)
            tvRefreshHashrateDelay.setText(Integer.toString(delay-1));

        updateHashrateRefreshDelayControls();
    }

    public void onIncreaseHashrateRefreshDelay() {
        int delay = Integer.parseInt(tvRefreshHashrateDelay.getText().toString());
        if(delay < Config.DefaultRefreshDelay)
            tvRefreshHashrateDelay.setText(Integer.toString(delay+1));

        updateHashrateRefreshDelayControls();
    }

    private void saveSettings() {
        // Validate address
        String address = edAddress.getText().toString().trim();

        if(address.isEmpty() || !Utils.verifyAddress(address)) {
            tilAddress.setErrorEnabled(true);
            tilAddress.setError(getResources().getString(R.string.invalidaddress));
            requestFocus(edAddress);
            return;
        }

        tilAddress.setErrorEnabled(false);
        tilAddress.setError(null);

        PoolItem selectedPoolItem = getSelectedPoolItem();

        Config.write(Config.CONFIG_SELECTED_POOL, selectedPoolItem.getKey().trim());
        Config.write(Config.CONFIG_CUSTOM_PORT, selectedPoolItem.getSelectedPort().trim());

        Config.write(Config.CONFIG_ADDRESS, address);

        Config.write(Config.CONFIG_USERNAME_PARAMETERS, edUsernameparameters.getText().toString().trim());

        String workername = edWorkerName.getText().toString().trim();
        if(workername.isEmpty()) {
            workername = Tools.getDeviceName();
        }

        Log.i(LOG_TAG,"Worker Name : " + workername);
        Config.write(Config.CONFIG_WORKERNAME, workername);
        edWorkerName.setText(workername);

        Config.write(Config.CONFIG_CORES, Integer.toString(sbCores.getProgress()+1));

        Config.write(Config.CONFIG_MAX_CPU_TEMP, Integer.toString(getCPUTemp()));
        Config.write(Config.CONFIG_MAX_BATTERY_TEMP, Integer.toString(getBatteryTemp()));
        Config.write(Config.CONFIG_COOLDOWN_THRESHOLD, Integer.toString(getCooldownTheshold()));

        Config.write(Config.CONFIG_HASHRATE_REFRESH_DELAY, tvRefreshHashrateDelay.getText().toString());

        Config.write(Config.CONFIG_DISABLE_TEMPERATURE_CONTROL, (swDisableTempControl.isChecked() ? "1" : "0"));

        Config.write(Config.CONFIG_PAUSE_ON_BATTERY, swPauseOnBattery.isChecked() ? "1" : "0");
        Config.write(Config.CONFIG_BATTERY_LEVEL_ENABLED, bDefineBatteryLevel ? "1" : "0");
        Config.write(Config.CONFIG_BATTERY_LEVEL, String.valueOf(nBatteryLevel));

        Config.write(Config.CONFIG_PAUSE_ON_NETWORK, swPauseOnNetwork.isChecked() ? "1" : "0");
        Config.write(Config.CONFIG_KEEP_SCREEN_ON_WHEN_MINING, swKeepScreenOnWhenMining.isChecked() ? "1" : "0");

        Config.write(Config.CONFIG_TEMPERATURE_UNIT, tgTemperatureUnit.getCheckedButtonId() == R.id.btnFarehnheit ? "F" : "C");
        Config.write(Config.CONFIG_SEND_DEBUG_INFO, swSendDebugInformation.isChecked() ? "1" : "0");

        Config.write(Config.CONFIG_INIT, "1");

        Utils.showToast(getContext(), "Settings Saved.", Toast.LENGTH_SHORT);

        MainActivity main = (MainActivity) getActivity();
        assert main != null;
        main.stopMining();
        main.loadSettings();

        main.updateStatsListener();
        main.updateUI();

        selectedPoolTmp = null;
    }

    private void onOpenPools() {
        Intent intent = new Intent(getActivity(), PoolActivity.class);
        intent.putExtra(PoolActivity.RequesterType, PoolActivity.REQUESTER_SETTINGS);
        startActivity(intent);
    }

    private Integer getCPUTemp() {
        return ((sbCPUTemp.getProgress()) * Utils.INCREMENT) + Utils.MIN_CPU_TEMP;
    }

    private Integer getBatteryTemp() {
        return ((sbBatteryTemp.getProgress()) * Utils.INCREMENT) + Utils.MIN_BATTERY_TEMP;
    }

    private Integer getCooldownTheshold() {
        return ((sbCooldown.getProgress()) * Utils.INCREMENT) + Utils.MIN_COOLDOWN;
    }

    private void updateCPUTemp() {
        int cpu_temp = getCPUTemp();
        tvCPUMaxTemp.setText(tgTemperatureUnit.getCheckedButtonId() == R.id.btnFarehnheit ? Integer.toString(Utils.convertCelciusToFahrenheit(cpu_temp)) : Integer.toString(cpu_temp));
    }

    private void updateBatteryTemp() {
        int battery_temp = getBatteryTemp();
        tvBatteryMaxTemp.setText(tgTemperatureUnit.getCheckedButtonId() == R.id.btnFarehnheit ? Integer.toString(Utils.convertCelciusToFahrenheit(battery_temp)) : Integer.toString(battery_temp));
    }

    private void updateCooldownThreshold() {
        tvCooldown.setText(Integer.toString(getCooldownTheshold()));
    }

    private void enableTemperatureControl(boolean enable) {
        sbCPUTemp.setEnabled(enable);
        sbBatteryTemp.setEnabled(enable);
        sbCooldown.setEnabled(enable);
    }

    private void startQrCodeActivity() {
        new IntentIntegrator(getActivity()).setOrientationLocked(false).setCaptureActivity(QrCodeScannerActivity.class).initiateScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        Context appContext = MainActivity.getContextOfApplication();
        if (requestCode == 100) {
            if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQrCodeActivity();
            }
            else {
                Utils.showToast(appContext,"Camera permission denied.", Toast.LENGTH_LONG);
            }
        }
    }

    public void updateAddress() {
        String address =  Config.read(Config.CONFIG_ADDRESS);
        if (edAddress == null || address.isEmpty()) {
            return;
        }

        edAddress.setText(address);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private PoolItem getSelectedPoolItem() {
        return selectedPoolTmp == null ? ProviderManager.getSelectedPool() : selectedPoolTmp;
    }

    @Override
    public void onResume() {
        super.onResume();

        pvSelectedPool.onFinishInflate();
    }

    @Override
    public void onDestroy() {
        selectedPoolTmp = null;

        super.onDestroy();
    }
}