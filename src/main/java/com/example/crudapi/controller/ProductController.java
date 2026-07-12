package.com.example.crudapi.controller;

import com.example.crudapi.model.Product;
import com.example.crudapi.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
//this annotation tells Spring that all return values from these methods should be automatically serialized into JSON.

@RequestMapping("/api/products")
// @RequestMapping sets the base URL path for all endpoints in this controller.

public class ProductController{

    private final ProductService service;

    // Endpoint: GET /api/products
    // Returns: List of products in JSON format with HTTP Status 200 OK
   public ProductController(ProductService service)
{   this.service=service;
}

@GetMapping
public List<Product> getProducts(){
    return service.getAllProducts();
}
//above function will return list of all products


@GetMapping("/{id}")
//Pathvariable will extract the variable id from url 
public Product getProductbyId(@PathVariable long id){
    return service.getProductbyId(id);


}
@PostMapping
//RequestBody will extract the body of the request convert json into java object
public createProduct(@RequestBody Product product)
{
    return service.createProduct(product);


}

@PutMapping
public UpdateProudct(@PathVariable long id, RequestBody Product product)
{
    return service.updateProduct(id,Product);
}
@DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteProduct(id);
    }
    

}
