package com.group1.team.autodiary.objects;


public class AssetInfo {
    public static final int DEPOSIT = 0;
    public static final int WITHDRAW = 1;

    private int depositOrWithdraw;
    private int sum;
    private String date;

    public AssetInfo(String depositOrWithdraw, String sum, String date) {
        if (depositOrWithdraw.equalsIgnoreCase("[입금]"))
            this.depositOrWithdraw = DEPOSIT;
        else



            this.depositOrWithdraw = WITHDRAW;

        this.sum = Integer.parseInt(sum.replace("원", ""));

        this.date = date;
    }

    public int getDepositOrWithdraw() { return depositOrWithdraw; }

    public int getSum() { return sum; }

    public String getDate() { return date; }
}
