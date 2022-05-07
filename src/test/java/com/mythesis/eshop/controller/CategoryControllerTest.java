package com.mythesis.eshop.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mythesis.eshop.dto.CategoryEntryDTO;
import com.mythesis.eshop.dto.CategoryInfoDTO;
import com.mythesis.eshop.dto.OrderEntryDTO;
import com.mythesis.eshop.dto.OrderInfoDTO;
import com.mythesis.eshop.exception.ApiError;
import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.repository.CategoryRepository;
import com.mythesis.eshop.model.service.CategoryService;
import com.mythesis.eshop.model.service.OrderService;
import com.mythesis.eshop.util.CategoryMapper;
import com.mythesis.eshop.util.OrderMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CategoryMapper categoryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetCategories_andReturn200() throws Exception {

        List<Category> categories = new ArrayList<>();
        categories.add(new Category());
        categories.add(new Category());
        when(categoryService.retrieveAll()).thenReturn(categories);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());

        verify(categoryMapper).toCategoryInfoDto(categories.get(0));
        verify(categoryMapper).toCategoryInfoDto(categories.get(1));

    }

    @Test
    void canNotGetCategories_andReturn403() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetCategory_andReturn200() throws Exception {
        Category category = new Category();
        CategoryInfoDTO expectedCategory = new CategoryInfoDTO();
        expectedCategory.setTitle("test");

        when(categoryService.retrieveById(eq(1L))).thenReturn(category);
        when(categoryMapper.toCategoryInfoDto(eq(category))).thenReturn(expectedCategory);

        MvcResult mvcResult = mockMvc.perform(get("/categories/{categoryId}", 1L))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedCategory)
        );
    }

    @Test
    void caNotGetCategory_andReturn403() throws Exception {
       mockMvc.perform(get("/categories/{categoryId}", 1L))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotGetCategoryWithWrongId_andReturn404() throws Exception {

        when(categoryService.retrieveById(eq(2L))).thenThrow(new NoSuchElementException("No such Category"));
        MvcResult mvcResult  = mockMvc.perform(get("/categories/{categoryId}", 2L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"No such Category");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
          objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canCreateCategory_andReturn200() throws Exception {
        CategoryEntryDTO categoryEntry = new CategoryEntryDTO();
        categoryEntry.setTitle("cat");
        Category category = new Category();
        category.setTitle("cat");

        when(categoryMapper.fromCategoryEntryDto(any())).thenReturn(category);

        mockMvc.perform(post("/categories")
                .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        ArgumentCaptor<CategoryEntryDTO> categoryEntryCaptor = ArgumentCaptor.forClass(CategoryEntryDTO.class);
        verify(categoryMapper, times(1)).fromCategoryEntryDto(categoryEntryCaptor.capture());
        verify(categoryService, times(1)).add(categoryCaptor.capture());

        assertThat(categoryEntryCaptor.getValue().getTitle())
                .isEqualTo("cat");
        assertThat(categoryCaptor.getValue().getTitle())
                .isEqualTo("cat");

    }

    @Test
    void canNotCreateCategory_andReturn403() throws Exception {
        CategoryEntryDTO categoryEntry = new CategoryEntryDTO();
        categoryEntry.setTitle("cat");

        mockMvc.perform(post("/categoriess")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryEntry)))
                .andExpect(status().isForbidden());


    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotCreateCategoryWhenViolations_andReturn400() throws Exception {
        CategoryEntryDTO categoryEntry = new CategoryEntryDTO();
        Set<ConstraintViolation<Order>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());


        when(categoryService.add(any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(post("/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryEntry)))
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
    void canNotCreateCategoryWhenIllegalArgs_andReturn400() throws Exception {

        CategoryEntryDTO categoryEntry = new CategoryEntryDTO();

        when(categoryService.add(any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(post("/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryEntry)))
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
    void canUpdateCategory_andReturn200() throws Exception {
        CategoryEntryDTO categoryEntry = new CategoryEntryDTO();
        categoryEntry.setTitle("cat");
        Category category = new Category();
        category.setTitle("cat");

        when(categoryMapper.fromCategoryEntryDto(any())).thenReturn(category);

        mockMvc.perform(put("/categories/{categoryId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<CategoryEntryDTO> categoryEntryCaptor = ArgumentCaptor.forClass(CategoryEntryDTO.class);

        verify(categoryMapper, times(1)).fromCategoryEntryDto(categoryEntryCaptor.capture());
        verify(categoryService, times(1)).update(idCaptor.capture(), categoryCaptor.capture());

        assertThat(categoryEntryCaptor.getValue().getTitle())
                .isEqualTo("cat");
        assertThat(categoryCaptor.getValue().getTitle())
                .isEqualTo("cat");
        assertThat(idCaptor.getValue())
                .isEqualTo(1L);

    }

    @Test
    void canNotUpdateCategory_andReturn403() throws Exception {
        CategoryEntryDTO categoryEntry = new CategoryEntryDTO();
        mockMvc.perform(put("/categories/{categoryId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryEntry)))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotUpdateCategoryWhenViolations_andReturn400() throws Exception {
        CategoryEntryDTO categoryEntry = new CategoryEntryDTO();
        Set<ConstraintViolation<Order>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());

        when(categoryService.update(eq(1L), any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(put("/categories/{categoryId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryEntry)))
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
    void canNotUpdateCategoryWhenIllegalArgs_andReturn400() throws Exception {
        CategoryEntryDTO categoryEntry = new CategoryEntryDTO();

        when(categoryService.update(eq(1L), any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(put("/categories/{categoryId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryEntry)))
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
    void canDeleteCategory_andReturn200() throws Exception {

        mockMvc.perform(delete("/categories/{categoryId}", 3L))
                .andExpect(status().isOk());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(categoryService, times(1)).deleteById(idCaptor.capture());

        assertThat(idCaptor.getValue())
                .isEqualTo(3L);

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotDeleteCategoryWhenNotADMIN_andReturn403() throws Exception {
        mockMvc.perform(delete("/categories/{categoryId}", 3L))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "ADMIN" })
    void canNotDeleteCategoryWhenWrongID_andReturn404() throws Exception {

        doThrow(new NoSuchElementException("Category does not exist")).when(categoryService).deleteById(3L);
       MvcResult mvcResult = mockMvc.perform(delete("/categories/{categoryId}", 3L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"Category does not exist");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }
}