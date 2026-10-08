package co.com.inventory.inventoryservice.usecases.impl;

import co.com.inventory.inventoryservice.models.ProductDto;
import co.com.inventory.inventoryservice.services.IInventoryService;
import co.com.inventory.inventoryservice.usecases.IInventoryPutUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.geom.IllegalPathStateException;
import java.util.List;

@Service
public class InventoryPutUseCase implements IInventoryPutUseCase {

    @Autowired
    IInventoryService service ;

    @Override
    public void update(ProductDto productDto) throws IllegalStateException, IllegalArgumentException {

        if(productDto == null)
            throw new IllegalArgumentException("El registro no esta especificado para actualizar!");

        List<ProductDto> products = service.getById(productDto.getId());

        if(products.isEmpty()){
            throw new IllegalPathStateException("El registro no existe!");
        }

        service.update(productDto);

    }


}
