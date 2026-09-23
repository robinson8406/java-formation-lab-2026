package co.com.inventory.inventoryservice.usecases.impl;

import co.com.inventory.inventoryservice.models.ProductDto;
import co.com.inventory.inventoryservice.services.IInventoryService;
import co.com.inventory.inventoryservice.usecases.IInventoryGetUseCase;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class InventoryGetUseCase implements IInventoryGetUseCase {


    private static IInventoryService service ;

    InventoryGetUseCase(IInventoryService service){
        this.service = service ;
    }


    @Override
    public List<ProductDto> read(Map<String, String> pathVariables) {

        if(pathVariables == null || pathVariables.isEmpty())
            return Collections.emptyList();

        if(pathVariables.containsKey("id"))
            return service.getById(pathVariables.get("id"));

        if(pathVariables.containsKey("name"))
            return service.getByName(pathVariables.get("name"));

        return  service.getAll();

    }
}
