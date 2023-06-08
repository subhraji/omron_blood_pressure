package com.example.testomron.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Omron HealthCare Inc
 */

public class PeripheralDevice implements Parcelable {

    private String mLocalName;
    private String mUuid;
    private String mModelName;
    private String mModelSeries;
    private int mSelectedUser;
    private int mCategory;

    public PeripheralDevice(String mLocalName,
                            String mUuid,
                            String mModelName,
                            String mModelSeries,
                            int mSelectedUser,
                            int mCategory) {
        this.mLocalName = mLocalName;
        this.mUuid = mUuid;
        this.mModelName = mModelName;
        this.mModelSeries = mModelSeries;
        this.mSelectedUser = mSelectedUser;
        this.mCategory = mCategory;
    }

    public PeripheralDevice(String mLocalName,
                            String mUuid,
                            int mSelectedUser,
                            int mCategory) {
        this.mLocalName = mLocalName;
        this.mUuid = mUuid;
        this.mSelectedUser = mSelectedUser;
        this.mCategory = mCategory;
    }


    protected PeripheralDevice(Parcel in) {
        mLocalName = in.readString();
        mUuid = in.readString();
        mModelName = in.readString();
        mModelSeries = in.readString();
        mSelectedUser = in.readInt();
        mCategory = in.readInt();
    }

    public static final Creator<PeripheralDevice> CREATOR = new Creator<PeripheralDevice>() {
        @Override
        public PeripheralDevice createFromParcel(Parcel in) {
            return new PeripheralDevice(in);
        }

        @Override
        public PeripheralDevice[] newArray(int size) {
            return new PeripheralDevice[size];
        }
    };

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String mUuid) {
        this.mUuid = mUuid;
    }

    public String getLocalName() {
        return mLocalName;
    }

    public void setLocalName(String mLocalName) {
        this.mLocalName = mLocalName;
    }

    public String getModelSeries() {
        return mModelSeries;
    }

    public void setModelName(String mModelName) {
        this.mModelName = mModelName;
    }

    public int getSelectedUser() {
        return mSelectedUser;
    }

    public void setSelectedUser(int mSelectedUser) {
        this.mSelectedUser = mSelectedUser;
    }

    public String getModelName() {
        return mModelName;
    }

    public void setModelSeries(String mModelSeries) {
        this.mModelSeries = mModelSeries;
    }

    public int getCategory() {
        return mCategory;
    }

    public void setCategory(int mCategory) {
        this.mCategory = mCategory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLocalName);
        dest.writeString(mUuid);
        dest.writeString(mModelName);
        dest.writeString(mModelSeries);
        dest.writeInt(mSelectedUser);
        dest.writeInt(mCategory);
    }
}

