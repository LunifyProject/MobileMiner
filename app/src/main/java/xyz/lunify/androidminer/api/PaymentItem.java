// Copyright (c) 2019, Mine2Gether.com
//
// Please see the included LICENSE file for more information.
//
// Copyright (c) 2025 Lunify
//
// Please see the included LICENSE file for more information.

package xyz.lunify.androidminer.api;

import java.util.Comparator;
import java.util.Date;

public class PaymentItem {

    public String mHash, mAmount, mFee;
    public Date mTimestamp;

    public PaymentItem() {
    }

    static public final Comparator<PaymentItem> PaymentComparator = new Comparator<PaymentItem>() {
        @Override
        public int compare(PaymentItem o1, PaymentItem o2) {
            return o1.mTimestamp.compareTo(o2.mTimestamp);
        }
    };
}
