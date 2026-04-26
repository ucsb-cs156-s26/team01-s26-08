package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

  @MockitoBean UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/UCSBDiningCommonsMenuItem/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all")).andExpect(status().is(200));
  }

  // Authorization tests for /api/UCSBDiningCommonsMenuItem/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Baked Pesto Pasta with Chicken")
                .param("station", "Entree Specials")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Baked Pesto Pasta with Chicken")
                .param("station", "Entree Specials")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsb_dining_commons_menu_items() throws Exception {

    // arrange
    UCSBDiningCommonsMenuItem menuItem1 =
        UCSBDiningCommonsMenuItem.builder()
            .id(1L)
            .diningCommonsCode("ortega")
            .name("Baked Pesto Pasta with Chicken")
            .station("Entree Specials")
            .build();

    UCSBDiningCommonsMenuItem menuItem2 =
        UCSBDiningCommonsMenuItem.builder()
            .id(2L)
            .diningCommonsCode("portola")
            .name("Cream of Broccoli Soup (v)")
            .station("Greens & Grains")
            .build();

    ArrayList<UCSBDiningCommonsMenuItem> expectedMenuItems = new ArrayList<>();
    expectedMenuItems.addAll(Arrays.asList(menuItem1, menuItem2));

    when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedMenuItems);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedMenuItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_ucsb_dining_commons_menu_item() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem menuItem =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("ortega")
            .name("Baked Pesto Pasta with Chicken")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.save(eq(menuItem))).thenReturn(menuItem);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBDiningCommonsMenuItem/post")
                    .param("diningCommonsCode", "ortega")
                    .param("name", "Baked Pesto Pasta with Chicken")
                    .param("station", "Entree Specials")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(menuItem);
    String expectedJson = mapper.writeValueAsString(menuItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "7"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    UCSBDiningCommonsMenuItem menuItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(7L)
            .diningCommonsCode("ortega")
            .name("Baked Pesto Pasta with Chicken")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(7L))).thenReturn(Optional.of(menuItem));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(menuItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange
    when(ucsbDiningCommonsMenuItemRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItem with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_ucsb_dining_commons_menu_item() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem originalMenuItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(67L)
            .diningCommonsCode("ortega")
            .name("Baked Pesto Pasta with Chicken")
            .station("Entree Specials")
            .build();

    UCSBDiningCommonsMenuItem editedMenuItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(67L)
            .diningCommonsCode("portola")
            .name("Cream of Broccoli Soup (v)")
            .station("Greens & Grains")
            .build();

    String requestBody = mapper.writeValueAsString(editedMenuItem);

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(67L)))
        .thenReturn(Optional.of(originalMenuItem));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(67L));
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(editedMenuItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_ucsb_dining_commons_menu_item_that_does_not_exist()
      throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem editedMenuItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(67L)
            .diningCommonsCode("portola")
            .name("Cream of Broccoli Soup (v)")
            .station("Greens & Grains")
            .build();

    String requestBody = mapper.writeValueAsString(editedMenuItem);

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(67L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 67 not found", json.get("message"));
  }

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    UCSBDiningCommonsMenuItem editedMenuItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(67L)
            .diningCommonsCode("portola")
            .name("Cream of Broccoli Soup (v)")
            .station("Greens & Grains")
            .build();

    String requestBody = mapper.writeValueAsString(editedMenuItem);

    mockMvc
        .perform(
            put("/api/UCSBDiningCommonsMenuItem")
                .param("id", "67")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(requestBody)
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_put() throws Exception {
    UCSBDiningCommonsMenuItem editedMenuItem =
        UCSBDiningCommonsMenuItem.builder()
            .id(67L)
            .diningCommonsCode("portola")
            .name("Cream of Broccoli Soup (v)")
            .station("Greens & Grains")
            .build();

    String requestBody = mapper.writeValueAsString(editedMenuItem);

    mockMvc
        .perform(
            put("/api/UCSBDiningCommonsMenuItem")
                .param("id", "67")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .content(requestBody)
                .with(csrf()))
        .andExpect(status().is(403));
  }
}
