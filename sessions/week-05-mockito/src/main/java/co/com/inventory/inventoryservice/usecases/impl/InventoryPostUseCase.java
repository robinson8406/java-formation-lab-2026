package co.com.inventory.inventoryservice.usecases.impl;

import co.com.inventory.inventoryservice.models.ProductDto;
import co.com.inventory.inventoryservice.services.IInventoryService;
import co.com.inventory.inventoryservice.usecases.IInventoryPostUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryPostUseCase implements IInventoryPostUseCase {

    @Autowired
    IInventoryService service ;

    @Override
    public String create(ProductDto productDto) throws IllegalStateException, IllegalArgumentException {

        if(productDto == null)
            throw new IllegalArgumentException("La información del producto esta vacía!") ;

        if(productDto.getName() == null){
            throw new IllegalArgumentException("El nombre del producto se encuentra vacío!") ;
        }

        List<ProductDto> products = new ArrayList<>();

        if(productDto.getId() != null){
            products = service.getById(productDto.getId());

            if (!products.isEmpty()){
                throw new IllegalStateException("El identificador del producto ya existe!");
            }
        }

        products = service.getByName(productDto.getName());

        if (!products.isEmpty()){
            throw new IllegalStateException("Ya existe un producto con ese nombre!");
        }

        String id = UUID.randomUUID().toString();

        productDto.setId(id);

        return service.create(productDto);

    }
}
