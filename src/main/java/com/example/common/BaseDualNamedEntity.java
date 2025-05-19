package com.example.common;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;

@MappedSuperclass

@Getter
@Setter
public abstract class BaseDualNamedEntity extends BaseNamedEntity {

    private String nameEn;

    @Override
    public String toString() {
        return "BaseDualNamedEntity{" +
                "nameEn='" + nameEn + '\'' +
                "} " + super.toString();
    }
}
