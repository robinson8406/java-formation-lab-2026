package co.com.inventory.inventoryservice.services;


import co.com.inventory.inventoryservice.models.ProductDto;

import java.util.List;

public interface IInventoryService {

    String create(ProductDto productDto) throws IllegalStateException;

    void update(ProductDto productDto) throws IllegalStateException ;


    List<ProductDto> getById(String id) ;

    List<ProductDto> getByName(String name) ;

    List<ProductDto> getAll() ;


}
