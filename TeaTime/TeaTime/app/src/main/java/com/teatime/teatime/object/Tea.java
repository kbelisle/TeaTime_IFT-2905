package com.teatime.teatime.object;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.EnumSet;

/**
 * Created by Kevin on 2018-04-04.
 */

@Entity
public class Tea {
    //Fields
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "teaName_EN")
    private String teaName_EN;

    @ColumnInfo(name = "teaName_FR")
    private String teaName_FR;

    @ColumnInfo(name = "type")
    private int tea_type;

    @ColumnInfo(name = "favourite")
    private boolean favourite;

    @ColumnInfo(name = "goodWith")
    private int goodWith;

    @ColumnInfo(name = "healthProperty")
    private int healthProperty;

    @ColumnInfo(name = "description_EN")
    private String description_EN;

    @ColumnInfo(name = "description_FR")
    private String description_FR;

    @ColumnInfo(name = "infusion")
    private long infusionTime;

    @ColumnInfo(name = "imgFileName")
    private String imgFileName;

    //Getters - Setters
    public long getId() {
        return this.id;
    }
    public String getImgFileName(){return this.imgFileName;}
    public void setImgFileName(String name){this.imgFileName = name;}
    //Used by the DAO, use the public getters-setters instead
    public String getTeaName_EN() { return this.teaName_EN; }
    public String getTeaName_FR() { return this.teaName_FR; }
    public String getDescription_EN() { return this.description_EN; }
    public String getDescription_FR() { return this.description_FR; }
    public int getTea_type() { return this.tea_type; }
    public int getGoodWith() { return this.goodWith; }
    public int getHealthProperty() { return this.healthProperty; }
    public void setId(long id) { this.id = id; }
    public void setTeaName_EN(String name) { this.teaName_EN = name; }
    public void setTeaName_FR(String name) { this.teaName_FR = name; }
    public void setDescription_EN(String description) { this.description_EN = description; }
    public void setDescription_FR(String description) { this.description_FR = description; }
    public void setTea_type(int value) { this.tea_type = value; }
    public void setGoodWith(int value) { this.goodWith = value; }
    public void setHealthProperty(int value) { this.healthProperty = value; }
    //END

    public String getName(String lang) {
        if (lang.equals("fr"))
            return this.teaName_FR;
        else
            return this.teaName_EN;
    }

    public void setName(String lang, String name) {
        if (lang.equals("fr"))
            this.teaName_FR = name;
        else
            this.teaName_EN = name;
    }

    public String getDescription(String lang) {
        if (lang.equals("fr"))
            return this.description_FR;
        else
            return this.description_EN;
    }

    public void setDescription(String lang, String description) {
        if (lang.equals("fr"))
            this.description_FR = description;
        else
            this.description_EN = description;
    }

    public EnumSet<TeaType> getTeaType() {
        return TeaType.getStatusFlags(this.tea_type);
    }

    public void setTeaType(EnumSet<TeaType> t) {
        this.tea_type = TeaType.getStatusValue(t);
    }

    public boolean getFavourite() {
        return this.favourite;
    }

    public void setFavourite(boolean v) {
        this.favourite = v;
    }

    public EnumSet<GoodWithType> getGoodWithFlags() {
        return GoodWithType.getStatusFlags(this.goodWith);
    }

    public void setGoodWithFlags(EnumSet<GoodWithType> g) {
        this.goodWith = GoodWithType.getStatusValue(g);
    }

    public EnumSet<HealthPropertyType> getHealthPropertyFlags() {
        return HealthPropertyType.getStatusFlags(this.healthProperty);
    }

    public void setHealthPropertyFlags(EnumSet<HealthPropertyType> h) {
        this.healthProperty = HealthPropertyType.getStatusValue(h);
    }

    public long getInfusionTime() { return this.infusionTime; }
    public void setInfusionTime(long value) { this.infusionTime = value; }

    //Constructors
    protected Tea() {}
    @Ignore
    public Tea(long id) {
        this.id = id;
    }
    @Ignore
    public Tea (String name_EN, String name_FR, String description_EN, String description_FR,
                boolean favourite, @NonNull EnumSet<TeaType> t, @NonNull EnumSet<HealthPropertyType> h,
                @NonNull EnumSet<GoodWithType> g, long infusionTime, String file_name) {
        this.teaName_EN = name_EN;
        this.teaName_FR = name_FR;
        this.favourite = favourite;
        this.tea_type = TeaType.getStatusValue(t);
        this.healthProperty = HealthPropertyType.getStatusValue(h);
        this.goodWith = GoodWithType.getStatusValue(g);
        this.description_EN = description_EN;
        this.description_FR = description_FR;
        this.infusionTime = infusionTime;
        this.imgFileName = file_name;
    }

    //Methods
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Tea))
            return false;

        Tea t = (Tea)obj;

        if( t.id == this.id &&
            t.teaName_FR.equals(this.teaName_FR) &&
            t.teaName_EN.equals(this.teaName_EN) &&
            t.tea_type == this.tea_type &&
            t.favourite == this.favourite &&
            t.goodWith == this.goodWith &&
            t.healthProperty == this.healthProperty &&
            t.infusionTime == this.infusionTime &&
            t.description_FR.equals(this.description_FR)&&
            t.description_EN.equals(this.description_EN) &&
            compareImgFileName(t))
            return true;
        else
            return false;
    }

    @Override
    public String toString() {
        return "ID : " + this.getId();
    }

    private boolean compareImgFileName(Tea t) {
        if(this.imgFileName != null && t.imgFileName != null) {
            return this.imgFileName.equals(t.imgFileName);
        }
        else {
            return this.imgFileName == null && t.imgFileName == null;
        }
    }
}
