package.com.example.crudapi.repository;


mport com.example.crud.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository //this is the layer that will have db queries

public class ProductRepository extends JpaRepository( Product,long){
 Optional <User> fint
    // 1. 'Product' specifies which Entity this repository manages.
// 2. 'Long' specifies the data type of the Primary Key (@Id).
// Spring automatically generates implementations for standard SQL methods:

    //save(Product p)
    //findById(id)
    //delete(p)
    //findAll()
    //update()

//but if you want to create custom db query u can create it here 

    
  List<Product>findProductByname(String name);
//@query notation = if you want to write sql query 

@Query("SELECT p FROM Product p where p.name = :name")
List<Product>findProductByname(String name);
//this above is the example of jpql queries  that references your  java entity class nd field names
Product findByIdCustom(Long id);
//this if db is returning signle item /row

}


