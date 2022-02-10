package com.mythesis.eshop.model.service;


import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.repository.UserRepository;
import com.mythesis.eshop.util.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Validator validator;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService underTest;



    @Test
    void canLoadUserByUsername(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("1234");
        user.setRole("ROLE_USER");

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        UserDetails userDet = underTest.loadUserByUsername(user.getUsername());

        assertThat(userDet.getUsername())
                .isEqualTo(user.getUsername());
        assertThat(userDet.getPassword())
                .isEqualTo(user.getPassword());
        assertThat(userDet.getAuthorities().toArray())
                .isEqualTo(authorities.toArray());
    }

    @Test
    void canNotLoadUserByNonExistingUsername(){
        when(userRepository.findByUsername("doesNotExist")).thenThrow(NoSuchElementException.class);

        assertThatThrownBy(
                () -> underTest.loadUserByUsername("doesNotExist")
        ).isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User does not exist");

    }

    @Test
    void canRetrieveAll(){
        underTest.retrieveAll();
        verify(userRepository).findAll();
    }

    @Test
    void canRetrieveById(){
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User user1 = underTest.retrieveById(1L);

        assertThat(user1.getId()).isEqualTo(user.getId());

    }

    @Test
    void canNotRetrieveByNonExistingId(){
        when(userRepository.findById(55L)).thenThrow(NoSuchElementException.class);
        assertThatThrownBy(
                () -> underTest.retrieveById(55L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("No such User");
    }

    @Test
    void canAdd(){
        User user = new User("test",
                "1234",
                "testName",
                "testLast",
                "somewhere",
                "6999999999");

        when(validator.validate(user)).thenReturn(new HashSet<>());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User user1 = underTest.add(user);

        assertThat(user1.getUsername()).isEqualTo(user.getUsername());

    }

    @Test
    void canNotAddExistingUsername(){
        User user = new User("test",
                "1234",
                "testName",
                "testLast",
                "somewhere",
                "6999999999");

        when(validator.validate(user)).thenReturn(new HashSet<>());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        assertThatThrownBy(
                ()-> underTest.add(user)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("username already exists");

    }

    @Test
    void canNotAddExistingPhone(){
        User user = new User("test",
                "1234",
                "testName",
                "testLast",
                "somewhere",
                "6999999999");

        when(validator.validate(user)).thenReturn(new HashSet<>());
        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.of(user));

        assertThatThrownBy(
                ()-> underTest.add(user)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("phone already exists");

    }

    @Test
    void canNotAddWithViolations(){
        User user = new User();

        when(validator.validate(user).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.add(user)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canUpdate(){

        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("1234");

        when(validator.validate(user).isEmpty()).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).copyProperties(user, user);
        when(userRepository.save(user)).thenReturn(user);

        User user1 = underTest.update(1L, user);

        assertThat(user1.getId()).isEqualTo(user.getId());

    }

    @Test
    void canNotUpdateWithExistingUsername(){

        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("1234");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        assertThatThrownBy(
                ()-> underTest.update(1L, user)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("username already exists");

    }

    @Test
    void canNotUpdateWithExistingPhone(){
        User user = new User();
        user.setPhone("6999999999");

        when(userRepository.findByPhone(user.getPhone())).thenReturn(Optional.of(user));

        assertThatThrownBy(
                ()-> underTest.update(1L, user)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("phone already exists");

    }

    @Test
    void canNotUpdateWithViolations(){
        User user = new User();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(validator.validate(user).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.update(1L, user)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canDelete(){
        underTest.delete(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void canNotDelete(){
        doThrow(EmptyResultDataAccessException.class).when(userRepository).deleteById(1L);
        assertThatThrownBy(
                ()-> underTest.delete(1L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("User does not exist");
    }

    @Test
    void canCheckUsername(){
        underTest.usernameExists("test");
        verify(userRepository).findByUsername("test");
    }

    @Test
    void canCheckPhone(){
        underTest.phoneExists("6999999999");
        verify(userRepository).findByPhone("6999999999");
    }


}