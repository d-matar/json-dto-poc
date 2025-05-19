package com.example.common;

import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseLookup extends  BaseDualNamedEntity implements ILookup {
    private boolean deprecated;
    private String type;
    private Integer order;


    @Override
    public String toString() {
        return "BaseLookup{" +
                "deprecated=" + deprecated +
                ", type='" + type + '\'' +
                ", order=" + order +
                "} " + super.toString();
    }
}
