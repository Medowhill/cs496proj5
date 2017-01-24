package com.group1.team.autodiary.objects;

import java.util.Date;

/**
 * Created by q on 2017-01-21.
 */

public class CallLog {
    private String phoneNumber;
    private String dir;
    private Date callDayTime;
    private long callDuration;
    private String callName;

    public CallLog(String phoneNumber, String dir, Date callDayTime, String callDuration, String callName) {
        this.phoneNumber = phoneNumber;
        this.dir = dir;
        this.callDayTime = callDayTime;
        this.callDuration = Long.valueOf(callDuration);
        this.callName = callName;
    }

    public String getPhoneNumber() { return phoneNumber; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDir() { return dir; }

    public void setDir(String dir) { this.dir = dir; }

    public Date getCallDayTime() { return callDayTime; }

    public void setCallDayTime(Date callDayTime) { this.callDayTime = callDayTime; }

    public long getCallDuration() { return callDuration; }

    public void setCallDuration(long callDuration) { this.callDuration = callDuration; }

    public String getName() { return callName; }

    public void setName(String callName) { this.callName = callName; }
}
