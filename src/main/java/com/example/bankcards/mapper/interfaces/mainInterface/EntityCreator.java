package com.example.bankcards.mapper.interfaces.mainInterface;

/**
 * Marker interface for entity creation from DTOs.
 * 
 * @param <T> type of entity to create
 */
public interface EntityCreator<T> {

    /**
     * Creates a new entity instance from DTO data.
     * 
     * @return new entity instance
     */
    T toEntity();

}