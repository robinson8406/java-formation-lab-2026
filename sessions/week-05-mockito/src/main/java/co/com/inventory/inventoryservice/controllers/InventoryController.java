package co.com.inventory.inventoryservice.controllers;

import co.com.inventory.inventoryservice.models.ProductDto;
import co.com.inventory.inventoryservice.usecases.IInventoryGetUseCase;
import co.com.inventory.inventoryservice.usecases.IInventoryPatchUseCase;
import co.com.inventory.inventoryservice.usecases.IInventoryPostUseCase;
import co.com.inventory.inventoryservice.usecases.IInventoryPutUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("inventory")
public class InventoryController {


    @Autowired
    IInventoryGetUseCase getUseCase ;

    @Autowired
    IInventoryPostUseCase postUseCase ;

    @Autowired
    IInventoryPutUseCase putUseCase ;

    @Autowired
    IInventoryPatchUseCase patchUseCase ;

    @GetMapping
    public ResponseEntity<List<ProductDto>> get(@RequestParam Map<String, String> optionals){
        if(optionals != null && !optionals.isEmpty()){

            List<ProductDto> data = getUseCase.read(optionals) ;
            if(data != null && !data.isEmpty())
                return  new ResponseEntity<>(data, HttpStatus.OK);

        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public  ResponseEntity<String> post(@RequestBody ProductDto product){
        if(product == null)
            return ResponseEntity.badRequest().build();

        try{
            String id = postUseCase.create(product) ;
            return  ResponseEntity.status(HttpStatus.CREATED).body(id);
        }catch (Exception e){

        }
        return  ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    @PutMapping
    public  ResponseEntity<Void> put(@RequestBody ProductDto product){

        if(product == null)
            return ResponseEntity.badRequest().build();

        try{
            putUseCase.update(product);
            return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }catch (Exception e){

        }
        return  ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    @PatchMapping("/{id}")
    public  ResponseEntity<String> patch(@PathVariable(name = "id") String id, @RequestBody ProductDto product){

        if(id == null || id.isEmpty())
            return ResponseEntity.notFound().build();

        if(product == null)
            return ResponseEntity.badRequest().build();

        try{
            patchUseCase.update(id, product);
            return  ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }catch (Exception e){

        }
        return  ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }



}
