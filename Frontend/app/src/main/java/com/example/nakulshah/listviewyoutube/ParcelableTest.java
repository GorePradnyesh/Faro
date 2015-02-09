package com.example.nakulshah.listviewyoutube;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nakulshah on 1/21/15.
 */
public class ParcelableTest implements Parcelable {

    String name;
    int status;

    public ParcelableTest(String name, int status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ParcelableTest(Parcel in){
        this.name = in.readString();
        this.status = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(status);
    }

    public static final Parcelable.Creator<ParcelableTest> CREATOR = new Parcelable.Creator<ParcelableTest>() {
        public ParcelableTest createFromParcel(Parcel in) {
            return new ParcelableTest(in);
        }

        public ParcelableTest[] newArray(int size) {
            return new ParcelableTest[size];

        }
    };
}
