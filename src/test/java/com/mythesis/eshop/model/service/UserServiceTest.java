package com.mythesis.eshop.model.service;

import com.mythesis.eshop.model.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService underTest;

    @BeforeEach
    void setUp(){
        //  underTest = new UserService(userRepository);
    }

    @Test
    void canRetrieveAllUsers(){
        underTest.retrieveAll();
        verify(userRepository).findAll();
    }

    void canRetrieveUserById(){

    }

}