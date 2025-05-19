package com.example.common;

import javax.persistence.MappedSuperclass;

@MappedSuperclass 
public abstract class BaseIdEntity  extends BaseEntity {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "Id")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseIdEntity that = (BaseIdEntity) o;
        return id != null && id.equals(that.getId());
    }
    
//    @Override
//    public int hashCode() {
//        return 13;
//    }

    @Override
    public int hashCode() {
    	  if (id==null)
          return super.hashCode();
    	  else 
    	  	return id.intValue();
    }

    @Override
    public String toString() {
        return "BaseIdEntity{" +
                "id=" + id +
                "} " + super.toString();
    }
}
