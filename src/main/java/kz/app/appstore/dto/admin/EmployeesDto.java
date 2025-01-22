package kz.app.appstore.dto.admin;

import kz.app.appstore.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeesDto {
    private Long id;
    private String name;
    private Role role;
    private String firstname;
    private String lastname;
}