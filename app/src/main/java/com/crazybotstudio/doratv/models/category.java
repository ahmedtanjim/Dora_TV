package com.crazybotstudio.doratv.models;

import android.os.Parcel;
import android.os.Parcelable;


public class category implements Parcelable {
    private String channelcatagory;
    public category() {}

    protected category(Parcel in) {
        channelcatagory = in.readString();
    }

    public static final Creator<channel> CREATOR = new Creator<channel>() {
        @Override
        public channel createFromParcel(Parcel in) {
            return new channel(in);
        }

        @Override
        public channel[] newArray(int size) {
            return new channel[size];
        }
    };

    public String getchannelcatagory()
    {
        return channelcatagory;
    }
    public void setchannelcatagory(String channelcatagory) { this.channelcatagory = channelcatagory; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(channelcatagory);
    }
}

