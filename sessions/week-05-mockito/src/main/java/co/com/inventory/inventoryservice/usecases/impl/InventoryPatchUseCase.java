package co.com.inventory.inventoryservice.usecases.impl;

import co.com.inventory.inventoryservice.models.ProductDto;
import co.com.inventory.inventoryservice.services.IInventoryService;
import co.com.inventory.inventoryservice.usecases.IInventoryPatchUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.geom.IllegalPathStateException;
import java.util.List;

@Service
public class InventoryPatchUseCase implements IInventoryPatchUseCase {

    @Autowired
    IInventoryService service ;

    @Override
    public void update(String id, ProductDto productDto) throws IllegalStateException, IllegalArgumentException {

        if(id == null || productDto == null)
            throw new IllegalArgumentException("El registro no esta especificado para actualizar!");

        List<ProductDto> producto = service.getById(id);

        if(producto.isEmpty()){
            throw new IllegalPathStateException("El registro no existe!");
        }

        if(productDto.getDescription() != null){
            producto.get(0).setDescription(productDto.getDescription());
        }

        if(productDto.getName() != null){
            producto.get(0).setName(productDto.getName());
        }

        if(productDto.getUnits() != null){
            producto.get(0).setUnits(productDto.getUnits());
        }

        if(productDto.getQuantity() != null){
            producto.get(0).setQuantity(productDto.getQuantity());
        }

        service.update(producto.get(0));

    }
}
