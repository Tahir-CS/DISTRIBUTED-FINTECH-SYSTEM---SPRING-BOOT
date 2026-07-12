//database  table mapping and have data blueprint 



package.com.example.crudapi.model
import lombok.* //for getters, setters and constructors
import jakarta.persistence.*; // for jpa related 



@Entity //this tells the spring that this class iw ill make below directly maps to db table

//@Table(name="products")//default can be classname or u could define table name here


@Data //this will give all the getters,setters,constructors
@NoArgsConstructor //this will generate default constructor
@AllArgsConstructor //this will generate constructor having all the fields


public class products{

    @id //this tells this column will be primary key 
    @GeneratedValue(strategy=GenerationType.IDENTITY) // this tells the database to auto generate the id 
   
   //input validation is simple u just defined annotation above fields like
    private long id;

    @NotBlank(message="Name is required")
    @Size(min=2,max=59,message="must be between 2 to 59 characters")
    private String name;

    @NotBlank(message="Name is required")
    @Positive(message="Price should be positive")
    private Double price;
}

