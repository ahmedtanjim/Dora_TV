package com.crazybotstudio.doratv.models;
import android.os.Parcel;
import android.os.Parcelable;

public class channel implements Parcelable {
    public String getChannelname() {
        return channelname;
    }

    public String getLink() {
        return link;
    }

    public String getChannellink() {
        return channellink;
    }

    public String getMulti() {
        return multi;
    }

    public String getType(){return type;}

    public static Parcelable.Creator<channel> getCREATOR() {
        return CREATOR;
    }

    private String channelname;
    private String multi;
    private String link;
    private String channellink;
    private String type;
    public channel() {}

    protected channel(Parcel in) {
        channelname = in.readString();
        link = in.readString();
        channellink = in.readString();
        multi = in.readString();
        type = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(channelname);
        dest.writeString(link);
        dest.writeString(channellink);
        dest.writeString(type);
        dest.writeString(multi);
    }
}

