package com.example.common;

/**
 * Interface of enums of lookups 
 * A typed Lookup should contain an field 'type' of type enum extending ILookupType
 * 
 * @author Ahmad Hamid
 *
 */
public  interface ILookupType<T extends Enum<T>>    {
  
 
	public String getCode()  ;
}
