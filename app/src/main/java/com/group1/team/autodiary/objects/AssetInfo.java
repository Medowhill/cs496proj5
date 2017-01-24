package com.group1.team.autodiary.objects;


import android.content.Context;

import com.group1.team.autodiary.R;

public class AssetInfo {
    public static final int DEPOSIT = 0;
    public static final int WITHDRAW = 1;

    private int depositOrWithdraw;
    private int sum;
    private String date;

    public AssetInfo(Context context, String depositOrWithdraw, String sum, String date) {
        if (depositOrWithdraw.equalsIgnoreCase(context.getString(R.string.deposit)))
            this.depositOrWithdraw = DEPOSIT;
        else
            this.depositOrWithdraw = WITHDRAW;

        this.sum = Integer.parseInt(sum.substring(0, sum.length() - 1));

        this.date = date;
    }

    public int getDepositOrWithdraw() { return depositOrWithdraw; }

    public int getSum() { return sum; }

    public String getDate() { return date; }
}
