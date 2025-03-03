// Copyright (c) 2025 Lunify
//
// Please see the included LICENSE file for more information.

package xyz.lunify.androidminer.api;

public interface IProviderListener {
    void onStatsChange(ProviderData data);

    boolean onEnabledRequest();
}
