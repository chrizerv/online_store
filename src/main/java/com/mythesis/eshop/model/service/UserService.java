package com.mythesis.eshop.model.service;

import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.repository.UserRepository;

import com.mythesis.eshop.util.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private Validator validator;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       Validator validator,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.validator = validator;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        try {
             user = userRepository.findByUsername(username).get();
        }catch (NoSuchElementException ex){
            throw new UsernameNotFoundException("User does not exist", ex);
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));

        return new org.springframework
                .security.core.userdetails
                .User(user.getUsername(), user.getPassword(), authorities);
    }

    public List<User> retrieveAll(){
        return userRepository.findAll();
    }

    public User retrieveById(Long userId){
        User user;
        try {
            user = userRepository.findById(userId).get();
        }catch (NoSuchElementException ex){
            throw new NoSuchElementException("No such User");
        }
        return user;
    }

    public User add(User user){

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        if (usernameExists(user.getUsername()))
            throw new IllegalArgumentException("username already exists");

        if (phoneExists(user.getPhone()))
            throw new IllegalArgumentException("phone already exists");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        return savedUser;
    }

    public User update(Long userId, User user){

        if (usernameExists(user.getUsername()))
            throw new IllegalArgumentException("username already exists");

        if (phoneExists(user.getPhone()))
            throw new IllegalArgumentException("phone already exists");

        User retrievedUser = retrieveById(userId);
        userMapper.copyProperties(user, retrievedUser);

        Set<ConstraintViolation<User>> violations = validator.validate(retrievedUser);
        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        User updatedUser = userRepository.save(retrievedUser);
       return  updatedUser;
    }

    public void delete(Long userId){
        try {
            userRepository.deleteById(userId);
        }catch (EmptyResultDataAccessException ex){
            throw new NoSuchElementException("User does not exist");
        }
    }

    public boolean usernameExists(String username){
        return userRepository
                .findByUsername(username)
                .isPresent();
    }

    public boolean phoneExists(String phone){
        return userRepository
                .findByPhone(phone)
                .isPresent();
    }

}
