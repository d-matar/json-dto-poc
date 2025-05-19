package com.example.common;

import javax.persistence.MappedSuperclass;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@Getter
@Setter
public abstract class BaseNamedEntity extends BaseIdEntity {

    private String name;

    @Override
    public String toString() {
        return "BaseNamedEntity{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
