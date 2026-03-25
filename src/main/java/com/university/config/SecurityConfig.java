// Security configuration with JWT authentication and BCrypt password hashing
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // Your custom JWT filter

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF 
            .csrf().disable()
            
            // 2. Set session to STATELESS (Since we are using JWT)
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            
            // 3. Define authorization rules
            .authorizeRequests()
            .antMatchers("/api/auth/**").permitAll() // Public endpoints
            .anyRequest().authenticated()           // Everything else (like /api/departments) needs a token
            .and()
            
            // 4. Add your JWT filter before the standard UsernamePassword filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
