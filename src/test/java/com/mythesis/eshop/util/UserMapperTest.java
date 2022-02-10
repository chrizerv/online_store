package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.UserInfoDTO;
import com.mythesis.eshop.dto.UserRegisterDTO;
import com.mythesis.eshop.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserMapper underTest;

    @Test
    void canConvertToUserInfoDto() {
        User user = new User();
        underTest.toUserInfoDto(user);

        verify(modelMapper).map(user, UserInfoDTO.class);

    }

    @Test
    void canConvertFromUserRegDto() {
        UserRegisterDTO user = new UserRegisterDTO();
        underTest.fromUserRegDto(user);

        verify(modelMapper).map(user, User.class);
    }

    @Test
    void canCopyProperties() {
        User uSource = new User();
        User uDest = new User();
        underTest.copyProperties(uSource, uDest);

        verify(modelMapper).map(uSource, uDest);

    }
}