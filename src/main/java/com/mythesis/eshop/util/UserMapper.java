package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.UserInfoDTO;
import com.mythesis.eshop.dto.UserLoginDTO;
import com.mythesis.eshop.dto.UserRegisterDTO;
import com.mythesis.eshop.model.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private ModelMapper modelMapper;

    @Autowired
    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public UserInfoDTO toUserInfoDto(User user){
        return modelMapper.map(user, UserInfoDTO.class);
    }

    public UserLoginDTO toUserLoginDto(Object user) {
        return modelMapper.map(user, UserLoginDTO.class);
    }

    public User fromUserRegDto(UserRegisterDTO user){
        return modelMapper.map(user, User.class);
    }

    public void copyProperties(User source, User destination){
        modelMapper.map(source,destination);
    }
}
