package kz.app.appstore.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private String bin;
    private String companyName;
}
