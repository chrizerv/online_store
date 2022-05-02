package com.mythesis.eshop.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mythesis.eshop.dto.UserInfoDTO;
import com.mythesis.eshop.dto.UserRegisterDTO;
import com.mythesis.eshop.exception.ApiError;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.service.UserService;
import com.mythesis.eshop.util.UserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@Import(UserService.class) // must seperate userdetails implementation
class UserControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetUsers_andReturn200() throws Exception {

        List<User> users = new ArrayList<>();
        users.add(new User());
        users.add(new User());
        when(userService.retrieveAll()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userMapper).toUserInfoDto(users.get(0));
        verify(userMapper).toUserInfoDto(users.get(1));

    }

    @Test
    void canNotGetUsers_andReturn403() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetUser_andReturn200() throws Exception {
        User user = new User();
        UserInfoDTO expectedUser = new UserInfoDTO();
        expectedUser.setUsername("t1");

        when(userService.retrieveById(eq(1L))).thenReturn(user);
        when(userMapper.toUserInfoDto(eq(user))).thenReturn(expectedUser);

        MvcResult mvcResult = mockMvc.perform(get("/users/{userId}", 1L))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedUser)
        );
    }

    @Test
    void caNotGetUser_andReturn403() throws Exception {
       mockMvc.perform(get("/users/{userId}", 1L))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotGetUserWithWrongId_andReturn404() throws Exception {

        when(userService.retrieveById(eq(2L))).thenThrow(new NoSuchElementException("No such User"));
        MvcResult mvcResult  = mockMvc.perform(get("/users/{userId}", 2L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"No such User");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
          objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canCreateUser_andReturn200() throws Exception {
        UserRegisterDTO userRegister = new UserRegisterDTO();
        userRegister.setUsername("t1");
        User user = new User();
        user.setUsername("t1");

        when(userMapper.fromUserRegDto(any())).thenReturn(user);

        mockMvc.perform(post("/users")
                .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isOk());


        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserRegisterDTO> userRegisterCaptor = ArgumentCaptor.forClass(UserRegisterDTO.class);
        verify(userMapper, times(1)).fromUserRegDto(userRegisterCaptor.capture());
        verify(userService, times(1)).add(userCaptor.capture());

        assertThat(userRegisterCaptor.getValue().getUsername())
                .isEqualTo("t1");
        assertThat(userCaptor.getValue().getUsername())
                .isEqualTo("t1");

    }

    @Test
    void canNotCreateUser_andReturn403() throws Exception {
        UserRegisterDTO userRegister = new UserRegisterDTO();
        userRegister.setUsername("t1");

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isForbidden());


    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotCreateUserWhenViolations_andReturn400() throws Exception {
        UserRegisterDTO userRegister = new UserRegisterDTO();
        Set<ConstraintViolation<User>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());


        when(userService.add(any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.BAD_REQUEST.value(),expectedViolations);
        expectedErrorResponse.setMessage("Fields validation error");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotCreateUserWhenIllegalArgs_andReturn400() throws Exception {

        UserRegisterDTO userRegister = new UserRegisterDTO();

        when(userService.add(any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.BAD_REQUEST.value(), "Field already exists");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canUpdateUser_andReturn200() throws Exception {
        UserRegisterDTO userRegister = new UserRegisterDTO();
        userRegister.setUsername("t1");
        User user = new User();
        user.setUsername("t1");

        when(userMapper.fromUserRegDto(any())).thenReturn(user);

        mockMvc.perform(put("/users/{userId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isOk());


        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<UserRegisterDTO> userRegisterCaptor = ArgumentCaptor.forClass(UserRegisterDTO.class);

        verify(userMapper, times(1)).fromUserRegDto(userRegisterCaptor.capture());
        verify(userService, times(1)).update(idCaptor.capture(), userCaptor.capture());

        assertThat(userRegisterCaptor.getValue().getUsername())
                .isEqualTo("t1");
        assertThat(userCaptor.getValue().getUsername())
                .isEqualTo("t1");
        assertThat(idCaptor.getValue())
                .isEqualTo(1L);

    }

    @Test
    void canNotUpdateUser_andReturn403() throws Exception {
        UserRegisterDTO userRegister = new UserRegisterDTO();
        mockMvc.perform(put("/users/{userId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotUpdateUserWhenViolations_andReturn400() throws Exception {
        UserRegisterDTO userRegister = new UserRegisterDTO();
        Set<ConstraintViolation<User>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());

        when(userService.update(eq(1L), any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(put("/users/{userId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.BAD_REQUEST.value(),expectedViolations);
        expectedErrorResponse.setMessage("Fields validation error");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotUpdateUserWhenIllegalArgs_andReturn400() throws Exception {
        UserRegisterDTO userRegister = new UserRegisterDTO();

        when(userService.update(eq(1L), any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(put("/users/{userId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.BAD_REQUEST.value(), "Field already exists");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "ADMIN" })
    void canDeleteUser_andReturn200() throws Exception {

        mockMvc.perform(delete("/users/{userId}", 3L))
                .andExpect(status().isOk());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).delete(idCaptor.capture());

        assertThat(idCaptor.getValue())
                .isEqualTo(3L);

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotDeleteUserWhenNotADMIN_andReturn403() throws Exception {
        mockMvc.perform(delete("/users/{userId}", 3L))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "ADMIN" })
    void canNotDeleteUserWhenWrongID_andReturn404() throws Exception {

        doThrow(new NoSuchElementException("User does not exist")).when(userService).delete(3L);
       MvcResult mvcResult = mockMvc.perform(delete("/users/{userId}", 3L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"User does not exist");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }
}