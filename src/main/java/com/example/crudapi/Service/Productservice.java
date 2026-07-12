package.com.example.crudapi.service

import com.example.crudapi.model.Product;
import com.example.crudapi.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service

public class ProductService{

    private final  ProductRepository repository;
    //constructor based dependency injection
        public ProductService(ProductRepository repository){
            this.repository=repository;

        }
//lets suppose a buinesss action of get all record
public List<Product> getAllProducts(){
    return repository.findAll();
}
    //lets suppose action of getting product by id
    public Product getProduct(long id){
        return repository.findById(id)
        .orElseThrow(()-> new RuntimeException("Product not found with this id "+id));
    }   
// Business Action: Insert new record
public Product createProduct(Product product){
    return repository.save(product);

}

public Product updateProduct(long id,Product updatedProduct){
    return Product 
}

//now i will create 1 for updating a product ofc i will receive a id of exisiting product and a  updated product

 public Product updateProduct(long id,Product updatedProduct)
 {
    //ensure product exist before 

    Product existing= getProductById(id);
//if not exist then 
       return null;

 //if exist then
 exisiting.setName(updatedProduct.getName());
 existing.setPrice(updatedProduct.getPrice());
 //save the updated product
 return repository.save(existing);

     
 }
// now i will create 1 for deleting a product 
  public void deleteProduct(long id){
    if(!repository.existsById(id)){
        return throw new RuntimeException("cannot delete cannot find product with id "+id);
    }
    repository.deleteById(id);
  }
  //as u can see we are using repository object made by the depenedncy injection of repostiory calss into service class
  //and we are usign that repo object to perform db operation here okay
}
