package com.resolvehub.dto.request;

import com.resolvehub.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateUserRequest {

    private Role role;

    private Boolean enabled;
}
