package demo.solar.tp.hamtv.adapter;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by filiperodrigues on 12/05/17.
 */

public class ItemModel implements Parcelable{
    private int id;
    private String image;
    private String name;

    protected ItemModel(Parcel in) {
        id = in.readInt();
        image = in.readString();
        name = in.readString();
    }

    public ItemModel() {

    }

    public ItemModel(int id, String image, String name) {
        this.id = id;
        this.image = image;
        this.name = name;
    }

    public static final Creator<ItemModel> CREATOR = new Creator<ItemModel>() {
        @Override
        public ItemModel createFromParcel(Parcel in) {
            return new ItemModel(in);
        }

        @Override
        public ItemModel[] newArray(int size) {
            return new ItemModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(image);
        dest.writeString(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }

        final ItemModel other = (ItemModel) obj;
        if (!this.image.equals(other.image)) {
            return false;
        }
        if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }

        return true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
