package.com.example.crudapi.model

import lombok.*
import jakarta.persistance

//for implementing security we need 
import org.springframework.security.core.userdetails.userdetails
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Table(name="user")
@NoArgsConstructor
@AllArgsConstructor

@Data
@Builder

public class user implements userdetails
{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

   @Columne(nullable=false,unique=true)
   private String username;
 
    @Column(nullable=false)
    private String password;


//@enumerated annotation tell the jpa to store the role as string 
// if we dont use @enumerated annotation then it will store the role as integer   
  @Enumerated(EnumType.STRING)
   private Role role; 
   
   //now to override methods we got from Userdetail interface

   @Override
   //first we will override authorities method

   public collection<?extends GrantedAuthority>
   getAuthority(){

    return List.of(
        new SimpleGrantedAuthority(role.name)
    )

    @Override
   public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override 
    public boolean isEnabled(){
        return true;

    }
    }
   }


    
    