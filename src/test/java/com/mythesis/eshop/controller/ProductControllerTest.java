package com.mythesis.eshop.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mythesis.eshop.dto.ProductEntryDTO;
import com.mythesis.eshop.dto.ProductInfoDTO;
import com.mythesis.eshop.dto.UserInfoDTO;
import com.mythesis.eshop.dto.UserRegisterDTO;
import com.mythesis.eshop.exception.ApiError;
import com.mythesis.eshop.model.entity.Product;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.service.ProductService;
import com.mythesis.eshop.model.service.UserService;
import com.mythesis.eshop.util.ProductMapper;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductMapper productMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetProducts_andReturn200() throws Exception {

        List<Product> products = new ArrayList<>();
        products.add(new Product());
        products.add(new Product());
        when(productService.retrieveAll()).thenReturn(products);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());

        verify(productMapper).toProductInfoDto(products.get(0));
        verify(productMapper).toProductInfoDto(products.get(1));

    }

    @Test
    void canNotGetProducts_andReturn403() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetProduct_andReturn200() throws Exception {
        Product product = new Product();
        ProductInfoDTO expectedProduct = new ProductInfoDTO();
        expectedProduct.setName("p1");

        when(productService.retrieveById(eq(1L))).thenReturn(product);
        when(productMapper.toProductInfoDto(eq(product))).thenReturn(expectedProduct);

        MvcResult mvcResult = mockMvc.perform(get("/products/{productId}", 1L))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedProduct)
        );
    }

    @Test
    void canNotGetProduct_andReturn403() throws Exception {
       mockMvc.perform(get("/products/{productId}", 1L))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotGetProductWithWrongId_andReturn404() throws Exception {

        when(productService.retrieveById(eq(2L))).thenThrow(new NoSuchElementException("No such Product"));
        MvcResult mvcResult  = mockMvc.perform(get("/products/{productId}", 2L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"No such Product");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
          objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canCreateProduct_andReturn200() throws Exception {
        ProductEntryDTO productEntry = new ProductEntryDTO();
        productEntry.setName("p1");
        Product product = new Product();
        product.setName("p1");

        when(productMapper.fromProductEntryDto(any())).thenReturn(product);

        mockMvc.perform(post("/products")
                .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        ArgumentCaptor<ProductEntryDTO> productEntryCaptor = ArgumentCaptor.forClass(ProductEntryDTO.class);
        verify(productMapper, times(1)).fromProductEntryDto(productEntryCaptor.capture());
        verify(productService, times(1)).add(productCaptor.capture());

        assertThat(productEntryCaptor.getValue().getName())
                .isEqualTo("p1");
        assertThat(productCaptor.getValue().getName())
                .isEqualTo("p1");

    }

    @Test
    void canNotCreateProduct_andReturn403() throws Exception {
        ProductEntryDTO productEntry = new ProductEntryDTO();
        productEntry.setName("p1");

        mockMvc.perform(post("/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productEntry)))
                .andExpect(status().isForbidden());


    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotCreateProductWhenViolations_andReturn400() throws Exception {
        ProductEntryDTO productEntry = new ProductEntryDTO();
        Set<ConstraintViolation<User>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());


        when(productService.add(any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(post("/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productEntry)))
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
    void canNotCreateProductWhenIllegalArgs_andReturn400() throws Exception {

        ProductEntryDTO productEntry = new ProductEntryDTO();

        when(productService.add(any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(post("/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productEntry)))
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
    void canUpdateProduct_andReturn200() throws Exception {
        ProductEntryDTO productEntry = new ProductEntryDTO();
        productEntry.setName("p1");
        Product product = new Product();
        product.setName("p1");

        when(productMapper.fromProductEntryDto(any())).thenReturn(product);

        mockMvc.perform(put("/products/{productId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<Product> userCaptor = ArgumentCaptor.forClass(Product.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<ProductEntryDTO> productEntryCaptor = ArgumentCaptor.forClass(ProductEntryDTO.class);

        verify(productMapper, times(1)).fromProductEntryDto(productEntryCaptor.capture());
        verify(productService, times(1)).update(idCaptor.capture(), userCaptor.capture());

        assertThat(productEntryCaptor.getValue().getName())
                .isEqualTo("p1");
        assertThat(userCaptor.getValue().getName())
                .isEqualTo("p1");
        assertThat(idCaptor.getValue())
                .isEqualTo(1L);

    }

    @Test
    void canNotUpdateProduct_andReturn403() throws Exception {
        ProductEntryDTO productEntry = new ProductEntryDTO();
        mockMvc.perform(put("/products/{productId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productEntry)))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotUpdateProductWhenViolations_andReturn400() throws Exception {
        ProductEntryDTO productEntry = new ProductEntryDTO();
        Set<ConstraintViolation<User>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());

        when(productService.update(eq(1L), any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(put("/products/{productId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productEntry)))
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
    void canNotUpdateProductWhenIllegalArgs_andReturn400() throws Exception {
        ProductEntryDTO productEntry = new ProductEntryDTO();

        when(productService.update(eq(1L), any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(put("/products/{productId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(productEntry)))
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
    void canDeleteProduct_andReturn200() throws Exception {

        mockMvc.perform(delete("/products/{productId}", 3L))
                .andExpect(status().isOk());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(productService, times(1)).delete(idCaptor.capture());

        assertThat(idCaptor.getValue())
                .isEqualTo(3L);

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotDeleteProductWhenNotADMIN_andReturn403() throws Exception {
        mockMvc.perform(delete("/products/{productId}", 3L))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "ADMIN" })
    void canNotDeleteProductWhenWrongID_andReturn404() throws Exception {

        doThrow(new NoSuchElementException("Product does not exist")).when(productService).delete(3L);
       MvcResult mvcResult = mockMvc.perform(delete("/products/{productId}", 3L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"Product does not exist");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }


}