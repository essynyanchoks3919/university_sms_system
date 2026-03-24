import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int age;
    private String email;
    
    @OneToMany(mappedBy = "student")
    private Set<Enrollment> enrollments;
    
    // Getters and Setters
}
