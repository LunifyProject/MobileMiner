/*
 * Copyright (c) 2018 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ////////////////
 *
 * Copyright (c) 2025 Lunify
 *
 * Please see the included LICENSE file for more information.*/

package xyz.lunify.androidminer.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import xyz.lunify.androidminer.R;
import xyz.lunify.androidminer.dialogs.HelpFragment;

import java.util.ArrayList;
import java.util.List;

public class Notice {
    private static final String PREFS_NAME = "notice";
    private static List<Notice> notices = null;

    public static final String NOTICE_SHOW_VAULT = "notice_vault";

    private static boolean bHandleOnClick = true;

    private static void init() {
        synchronized (Notice.class) {
            if (notices != null) return;
            notices = new ArrayList<>();

            notices.add(
                    new Notice(NOTICE_SHOW_VAULT,
                            R.string.notice_no_wallet,
                            R.string.ok,
                            1)
            );
        }
    }

    public static void showAll(ViewGroup parent, String selector, boolean handleOnClick) {
        bHandleOnClick = handleOnClick;
        showAll(parent, selector);
    }

    public static void showAll(ViewGroup parent, String selector) {
        if (notices == null) init();
        for (Notice notice : notices) {
            if (notice.id.matches(selector))
                notice.show(parent);
        }
    }

    private final String id;
    private final int textResId;
    private final int helpResId;
    private final int defaultCount;
    private transient int count = -1;

    private Notice(final String id, final int textResId, final int helpResId, final int defaultCount) {
        this.id = id;
        this.textResId = textResId;
        this.helpResId = helpResId;
        this.defaultCount = defaultCount;
    }

    // show this notice as a child of the given parent view
    // NB: it assumes the parent is in a Fragment
    private void show(final ViewGroup parent) {
        final Context context = parent.getContext();
        if (getCount(context) <= 0) return; // don't add it

        final LinearLayout ll = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.template_notice, parent, false);

        ((TextView) ll.findViewById(R.id.tvNotice)).setText(textResId);

        if(bHandleOnClick) {
            final FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HelpFragment.display(fragmentManager, helpResId);
                }
            });
        }

        ImageView ib = ll.findViewById(R.id.ivClose);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll.setVisibility(View.GONE);
                decCount(context);
            }
        });
        parent.addView(ll);
    }

    private int getCount(final Context context) {
        count = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(id, defaultCount);
        return count;
    }

    private void decCount(final Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (count < 0) // not initialized yet
            count = prefs.getInt(id, defaultCount);
        if (count > 0)
            prefs.edit().putInt(id, count - 1).apply();
    }
}