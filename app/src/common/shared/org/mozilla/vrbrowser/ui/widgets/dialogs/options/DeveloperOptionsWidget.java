/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.vrbrowser.ui.widgets.dialogs.options;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.audio.AudioEngine;
import org.mozilla.vrbrowser.browser.SessionStore;
import org.mozilla.vrbrowser.browser.SettingsStore;
import org.mozilla.vrbrowser.ui.views.UIButton;
import org.mozilla.vrbrowser.ui.views.settings.ButtonSetting;
import org.mozilla.vrbrowser.ui.views.settings.RadioGroupSetting;
import org.mozilla.vrbrowser.ui.views.settings.SwitchSetting;
import org.mozilla.vrbrowser.ui.widgets.dialogs.RestartDialogWidget;
import org.mozilla.vrbrowser.ui.widgets.UIWidget;
import org.mozilla.vrbrowser.ui.widgets.WidgetPlacement;

import static org.mozilla.vrbrowser.utils.ServoUtils.isServoAvailable;

public class DeveloperOptionsWidget extends UIWidget {

    private AudioEngine mAudio;
    private UIButton mBackButton;

    private SwitchSetting mRemoteDebuggingSwitch;
    private SwitchSetting mConsoleLogsSwitch;
    private SwitchSetting mEnvOverrideSwitch;
    private SwitchSetting mMultiprocessSwitch;
    private SwitchSetting mServoSwitch;

    private RadioGroupSetting mEnvironmentsRadio;
    private RadioGroupSetting mPointerColorRadio;

    private ButtonSetting mResetButton;

    private int mRestartDialogHandle = -1;
    private ScrollView mScrollbar;

    public DeveloperOptionsWidget(Context aContext) {
        super(aContext);
        initialize(aContext);
    }

    public DeveloperOptionsWidget(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs);
        initialize(aContext);
    }

    public DeveloperOptionsWidget(Context aContext, AttributeSet aAttrs, int aDefStyle) {
        super(aContext, aAttrs, aDefStyle);
        initialize(aContext);
    }

    private void initialize(Context aContext) {
        inflate(aContext, R.layout.options_developer, this);

        mAudio = AudioEngine.fromContext(aContext);

        mBackButton = findViewById(R.id.backButton);
        mBackButton.setOnClickListener((OnClickListener) view -> {
            if (mAudio != null) {
                mAudio.playSound(AudioEngine.Sound.CLICK);
            }

            onDismiss();
        });

        mRemoteDebuggingSwitch = findViewById(R.id.remote_debugging_switch);
        mRemoteDebuggingSwitch.setOnCheckedChangeListener(mRemoteDebuggingListener);
        setRemoteDebugging(SettingsStore.getInstance(getContext()).isRemoteDebuggingEnabled(), false);

        mConsoleLogsSwitch = findViewById(R.id.show_console_switch);
        mConsoleLogsSwitch.setOnCheckedChangeListener(mConsoleLogsListener);
        setConsoleLogs(SettingsStore.getInstance(getContext()).isConsoleLogsEnabled(), false);

        mEnvOverrideSwitch = findViewById(R.id.env_override_switch);
        mEnvOverrideSwitch.setOnCheckedChangeListener(mEnvOverrideListener);
        setEnvOverride(SettingsStore.getInstance(getContext()).isEnvironmentOverrideEnabled());

        mMultiprocessSwitch = findViewById(R.id.multiprocess_switch);
        mMultiprocessSwitch.setOnCheckedChangeListener(mMultiprocessListener);
        setMultiprocess(SettingsStore.getInstance(getContext()).isMultiprocessEnabled(), false);

        mServoSwitch = findViewById(R.id.servo_switch);
        if (!isServoAvailable()) {
            mServoSwitch.setVisibility(View.GONE);
        } else {
            mServoSwitch.setOnCheckedChangeListener(mServoListener);
            setServo(SettingsStore.getInstance(getContext()).isServoEnabled(), false);
        }

        String env = SettingsStore.getInstance(getContext()).getEnvironment();
        mEnvironmentsRadio = findViewById(R.id.environment_radio);
        mEnvironmentsRadio.setOnCheckedChangeListener(mEnvsListener);
        setEnv(mEnvironmentsRadio.getIdForValue(env), false);

        int color = SettingsStore.getInstance(getContext()).getPointerColor();
        mPointerColorRadio = findViewById(R.id.pointer_radio);
        mPointerColorRadio.setOnCheckedChangeListener(mPointerColorListener);
        setPointerColor(mPointerColorRadio.getIdForValue(color), false);

        mResetButton = findViewById(R.id.resetButton);
        mResetButton.setOnClickListener(mResetListener);

        mScrollbar = findViewById(R.id.scrollbar);
    }

    @Override
    protected void initializeWidgetPlacement(WidgetPlacement aPlacement) {
        aPlacement.visible = false;
        aPlacement.width =  WidgetPlacement.dpDimension(getContext(), R.dimen.developer_options_width);
        aPlacement.height = WidgetPlacement.dpDimension(getContext(), R.dimen.developer_options_height);
        aPlacement.parentAnchorX = 0.5f;
        aPlacement.parentAnchorY = 0.5f;
        aPlacement.anchorX = 0.5f;
        aPlacement.anchorY = 0.5f;
        aPlacement.translationY = WidgetPlacement.unitFromMeters(getContext(), R.dimen.restart_dialog_world_y);
        aPlacement.translationZ = WidgetPlacement.unitFromMeters(getContext(), R.dimen.restart_dialog_world_z);
    }

    @Override
    public void show() {
        super.show();

        mScrollbar.scrollTo(0, 0);
    }

    private void showRestartDialog() {
        hide();

        UIWidget widget = getChild(mRestartDialogHandle);
        if (widget == null) {
            widget = createChild(RestartDialogWidget.class, false);
            mRestartDialogHandle = widget.getHandle();
            widget.setDelegate(() -> onRestartDialogDismissed());
        }

        widget.show();
    }

    private void onRestartDialogDismissed() {
       show();
    }

    private SwitchSetting.OnCheckedChangeListener mRemoteDebuggingListener = (compoundButton, value, doApply) -> {
        setRemoteDebugging(value, doApply);
    };

    private SwitchSetting.OnCheckedChangeListener mConsoleLogsListener = (compoundButton, value, doApply) -> {
        setConsoleLogs(value, doApply);
    };

    private SwitchSetting.OnCheckedChangeListener mEnvOverrideListener = (compoundButton, value, doApply) -> {
        setEnvOverride(value);
        showRestartDialog();
    };

    private SwitchSetting.OnCheckedChangeListener mMultiprocessListener = (compoundButton, value, doApply) -> {
        setMultiprocess(value, doApply);
    };

    private SwitchSetting.OnCheckedChangeListener mServoListener = (compoundButton, b, doApply) -> {
        setServo(b, true);
    };

    private RadioGroupSetting.OnCheckedChangeListener mEnvsListener = (radioGroup, checkedId, doApply) -> {
        setEnv(checkedId, doApply);
    };

    private RadioGroupSetting.OnCheckedChangeListener mPointerColorListener = (radioGroup, checkedId, doApply) -> {
        setPointerColor(checkedId, doApply);
    };

    private OnClickListener mResetListener = (view) -> {
        boolean restart = false;
        if (mRemoteDebuggingSwitch.isChecked() != SettingsStore.REMOTE_DEBUGGING_DEFAULT) {
            setRemoteDebugging(SettingsStore.REMOTE_DEBUGGING_DEFAULT, true);
            restart = true;
        }

        setConsoleLogs(SettingsStore.CONSOLE_LOGS_DEFAULT, true);
        setMultiprocess(SettingsStore.MULTIPROCESS_DEFAULT, true);
        setServo(SettingsStore.SERVO_DEFAULT, true);

        if (mEnvOverrideSwitch.isChecked() != SettingsStore.ENV_OVERRIDE_DEFAULT) {
            setEnvOverride(SettingsStore.ENV_OVERRIDE_DEFAULT);
            restart = true;
        }

        if (!mEnvironmentsRadio.getValueForId(mEnvironmentsRadio.getCheckedRadioButtonId()).equals(SettingsStore.ENV_DEFAULT)) {
            setEnv(mEnvironmentsRadio.getIdForValue(SettingsStore.ENV_DEFAULT), true);
        }

        if (restart)
            showRestartDialog();
    };

    private void setRemoteDebugging(boolean value, boolean doApply) {
        mRemoteDebuggingSwitch.setOnCheckedChangeListener(null);
        mRemoteDebuggingSwitch.setValue(value, doApply);
        mRemoteDebuggingSwitch.setOnCheckedChangeListener(mRemoteDebuggingListener);

        SettingsStore.getInstance(getContext()).setRemoteDebuggingEnabled(value);

        if (doApply) {
            SessionStore.get().setRemoteDebugging(value);
        }
    }

    private void setConsoleLogs(boolean value, boolean doApply) {
        mConsoleLogsSwitch.setOnCheckedChangeListener(null);
        mConsoleLogsSwitch.setValue(value, doApply);
        mConsoleLogsSwitch.setOnCheckedChangeListener(mConsoleLogsListener);

        SettingsStore.getInstance(getContext()).setConsoleLogsEnabled(value);

        if (doApply) {
            SessionStore.get().setConsoleOutputEnabled(value);
        }
    }

    private void setEnvOverride(boolean value) {
        mEnvOverrideSwitch.setOnCheckedChangeListener(null);
        mEnvOverrideSwitch.setValue(value, false);
        mEnvOverrideSwitch.setOnCheckedChangeListener(mEnvOverrideListener);

        SettingsStore.getInstance(getContext()).setEnvironmentOverrideEnabled(value);
    }

    private void setMultiprocess(boolean value, boolean doApply) {
        mMultiprocessSwitch.setOnCheckedChangeListener(null);
        mMultiprocessSwitch.setValue(value, false);
        mMultiprocessSwitch.setOnCheckedChangeListener(mMultiprocessListener);

        SettingsStore.getInstance(getContext()).setMultiprocessEnabled(value);

        if (doApply) {
            SessionStore.get().setMultiprocess(value);
        }
    }

    private void setServo(boolean value, boolean doApply) {
        mServoSwitch.setOnCheckedChangeListener(null);
        mServoSwitch.setValue(value, false);
        mServoSwitch.setOnCheckedChangeListener(mServoListener);

        SettingsStore.getInstance(getContext()).setServoEnabled(value);

        if (doApply) {
            SessionStore.get().setServo(value);
        }
    }

    private void setEnv(int checkedId, boolean doApply) {
        mEnvironmentsRadio.setOnCheckedChangeListener(null);
        mEnvironmentsRadio.setChecked(checkedId, doApply);
        mEnvironmentsRadio.setOnCheckedChangeListener(mEnvsListener);

        SettingsStore.getInstance(getContext()).setEnvironment((String) mEnvironmentsRadio.getValueForId(checkedId));

        if (doApply) {
            mWidgetManager.updateEnvironment();
        }
    }

    private void setPointerColor(int checkedId, boolean doApply) {
        mPointerColorRadio.setOnCheckedChangeListener(null);
        mPointerColorRadio.setChecked(checkedId, doApply);
        mPointerColorRadio.setOnCheckedChangeListener(mPointerColorListener);

        SettingsStore.getInstance(getContext()).setPointerColor((int)mPointerColorRadio.getValueForId(checkedId));

        if (doApply) {
            mWidgetManager.updatePointerColor();
        }
    }

}
