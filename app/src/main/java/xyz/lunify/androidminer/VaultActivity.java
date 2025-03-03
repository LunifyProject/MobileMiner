// Copyright (c) 2025 Lunify
//
// Please see the included LICENSE file for more information.

package xyz.lunify.androidminer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class VaultActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }

        setContentView(R.layout.fragment_vault);
    }

    public void onCloseVault(View view) {
        super.onBackPressed();
    }

    public void downloadLunifyVault(View view) {
        Uri uri = Uri.parse(getResources().getString(R.string.lunify_vault_url));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}