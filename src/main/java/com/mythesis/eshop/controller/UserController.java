package com.mythesis.eshop.controller;

import com.mythesis.eshop.dto.UserInfoDTO;
import com.mythesis.eshop.dto.UserRegisterDTO;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.service.UserService;
import com.mythesis.eshop.util.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }


    @GetMapping
    public List<UserInfoDTO> getUsers(){
        return userService.retrieveAll().stream()
                .map(userMapper::toUserInfoDto)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/{userId}")
    public UserInfoDTO getUser(@PathVariable("userId") Long userId){
        return userMapper.toUserInfoDto(userService.retrieveById(userId));
    }

    @PostMapping
    public UserInfoDTO createUser(@RequestBody UserRegisterDTO userData) {
        User user = userMapper.fromUserRegDto(userData);

        return userMapper.toUserInfoDto(userService.add(user));

    }

    @PutMapping(path = "/{userId}")
    public UserInfoDTO updateUser(@PathVariable("userId") Long userId,
                                  @RequestBody UserRegisterDTO userData){
        User user = userMapper.fromUserRegDto(userData);

        return userMapper.toUserInfoDto(userService.update(userId, user));
    }

    @DeleteMapping(path = "/{userId}")
    public void deleteUser (@PathVariable("userId") Long userId){
        userService.delete(userId);
    }
}
