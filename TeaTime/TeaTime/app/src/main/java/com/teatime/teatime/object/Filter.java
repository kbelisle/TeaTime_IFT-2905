package com.teatime.teatime.object;

import android.content.Context;

import java.util.EnumSet;
import java.util.List;

public class Filter {
    //Fields
    private String name;
    private int teaType;
    private int goodWith;
    private int healthProperty;

    //Getters - Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnumSet<TeaType> getTeaType() {
        return TeaType.getStatusFlags(this.teaType);
    }

    public void setTeaType(EnumSet<TeaType> teaType) {
        this.teaType = TeaType.getStatusValue(teaType);
    }

    public EnumSet<GoodWithType> getGoodWith() {
        return GoodWithType.getStatusFlags(this.goodWith);
    }

    public void setGoodWith(EnumSet<GoodWithType> goodWith) {
        this.goodWith = GoodWithType.getStatusValue(goodWith);
    }

    public EnumSet<HealthPropertyType> getHealthProperty() {
        return HealthPropertyType.getStatusFlags(this.healthProperty);
    }

    public void setHealthProperty(EnumSet<HealthPropertyType> healthProperty) {
        this.healthProperty = HealthPropertyType.getStatusValue(healthProperty);
    }

    //Constructor
    public Filter(String name, EnumSet<TeaType> teaTypes, EnumSet<GoodWithType> goodWithTypes,
                  EnumSet<HealthPropertyType> healthPropertyTypes) {
        this.name = name;
        this.setTeaType(teaTypes);
        this.setGoodWith(goodWithTypes);
        this.setHealthProperty(healthPropertyTypes);
    }

    //Methods
    public List<Tea> getTeasFromFilter(Context ctx, String lang) {
        if(lang.equals("fr")) {
            return ApplicationHelper.getDB(ctx).teaDao().getAllByFilter_FR(this.name,this.teaType,this.goodWith,this.healthProperty);
        }
        else {
            return ApplicationHelper.getDB(ctx).teaDao().getAllByFilter_EN(this.name,this.teaType,this.goodWith,this.healthProperty);
        }
    }
}
