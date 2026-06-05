package com.invision.web.Invision.mapper;

import com.invision.web.Invision.dto.UserRegistrationDto;
import com.invision.web.Invision.dto.UserResponseDTO;
import com.invision.web.Invision.model.Role;
import com.invision.web.Invision.model.User;

public class UserMapper {

    public User UserRegistrationDTOToUser(UserRegistrationDto regDTO){

        return User.builder().name(regDTO.name()).
                department(regDTO.department()).
                email(regDTO.email()).
                role(regDTO.role()).
                password(regDTO.password()).
                build();
    }

    public UserResponseDTO UserToUserResponseDTO(User user){
        return new UserResponseDTO(String.valueOf(user.getUserId()), user.getName(), user.getDepartment(), user.getEmail(), user.getRole());


    }

    public User UserRegistrationDTOToBorrower(UserRegistrationDto regDTO){

        return User.builder().name(regDTO.name()).
                department(regDTO.department()).
                email(regDTO.email()).
                role(Role.BORROWER).
                password(regDTO.password()).
                build();
    }



}
