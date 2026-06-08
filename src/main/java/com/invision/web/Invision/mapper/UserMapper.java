package com.invision.web.Invision.mapper;

import com.invision.web.Invision.dto.UserRegistrationDTO;
import com.invision.web.Invision.dto.UserResponseDTO;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.Role;
import com.invision.web.Invision.model.User;

public class UserMapper {

    public User UserRegistrationDTOToUser(UserRegistrationDTO regDTO){

        return User.builder().name(regDTO.name()).
                department(Department.valueOf(regDTO.department())).
                email(regDTO.email()).
                role(regDTO.role()).
                password(regDTO.password()).
                build();
    }

    public UserResponseDTO UserToUserResponseDTO(User user){
        return new UserResponseDTO(String.valueOf(user.getUserId()), user.getName(), String.valueOf(user.getDepartment()), user.getEmail(), user.getRole());


    }

    public User UserRegistrationDTOToBorrower(UserRegistrationDTO regDTO){

        return User.builder().name(regDTO.name()).
                department(Department.valueOf(regDTO.department())).
                email(regDTO.email()).
                role(Role.BORROWER).
                password(regDTO.password()).
                build();
    }



}
