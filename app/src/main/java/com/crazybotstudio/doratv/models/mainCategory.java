package com.crazybotstudio.doratv.models;

import android.os.Parcel;
import android.os.Parcelable;

public class mainCategory implements Parcelable{
    private String mc;
    public mainCategory() {}

    protected mainCategory(Parcel in) {
        mc = in.readString();
    }

    public static final Parcelable.Creator<channel> CREATOR = new Parcelable.Creator<channel>() {
        @Override
        public channel createFromParcel(Parcel in) {
            return new channel(in);
        }

        @Override
        public channel[] newArray(int size) {
            return new channel[size];
        }
    };

    public String getmc()
    {
        return mc;
    }
    public void setmc(String channelcatagory) { this.mc = channelcatagory; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mc);
    }
}
