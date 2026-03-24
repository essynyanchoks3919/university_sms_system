// Repository interfaces using Spring Data JPA
// Example:
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByLastName(String lastName);
}