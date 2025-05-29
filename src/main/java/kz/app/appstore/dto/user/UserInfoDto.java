package kz.app.appstore.dto.user;

import kz.app.appstore.enums.Role;
import kz.app.appstore.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String bin;
    private String companyName;
    private Role role;
    private UserType userType;
    private boolean active;
}
