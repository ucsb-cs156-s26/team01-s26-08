package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/UCSBOrganization/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/UCSBOrganization").param("id", "ZPRC").with(csrf()))
        .andExpect(status().is(403));
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization").param("id", "ZPRC")).andExpect(status().is(403));
  }

  // Authorization tests for /api/UCSBOrganization/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "ZPRC")
                .param("orgTranslationShort", "ZPRC short")
                .param("orgTranslation", "ZPRC full translation")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "ZPRC")
                .param("orgTranslationShort", "ZPRC short")
                .param("orgTranslation", "ZPRC full translation")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsb_organizations() throws Exception {

    // arrange
    UCSBOrganization org1 =
        UCSBOrganization.builder()
            .orgCode("ZPRC")
            .orgTranslationShort("ZPRC short")
            .orgTranslation("ZPRC full translation")
            .inactive(false)
            .build();

    UCSBOrganization org2 =
        UCSBOrganization.builder()
            .orgCode("TEST")
            .orgTranslationShort("TEST short")
            .orgTranslation("TEST full translation")
            .inactive(true)
            .build();

    ArrayList<UCSBOrganization> expectedOrgs = new ArrayList<>();
    expectedOrgs.addAll(Arrays.asList(org1, org2));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrgs);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().isOk()).andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrgs);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgCode("ZPRC")
            .orgTranslationShort("ZPRC short")
            .orgTranslation("ZPRC full translation")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("ZPRC"))).thenReturn(Optional.of(org));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBOrganization").param("id", "ZPRC"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById(eq("ZPRC"));
    String expectedJson = mapper.writeValueAsString(org);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange
    when(ucsbOrganizationRepository.findById(eq("NOPE"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBOrganization").param("id", "NOPE"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById(eq("NOPE"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id NOPE not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_ucsb_organization() throws Exception {
    // arrange
    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgCode("ZPRC")
            .orgTranslationShort("ZPRC short")
            .orgTranslation("ZPRC full translation")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(org))).thenReturn(org);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBOrganization/post")
                    .param("orgCode", "ZPRC")
                    .param("orgTranslationShort", "ZPRC short")
                    .param("orgTranslation", "ZPRC full translation")
                    .param("inactive", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).save(org);
    String expectedJson = mapper.writeValueAsString(org);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_existing_ucsb_organization() throws Exception {
    // arrange
    UCSBOrganization org =
        UCSBOrganization.builder()
            .orgCode("ZPRC")
            .orgTranslationShort("ZPRC short")
            .orgTranslation("ZPRC full translation")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("ZPRC"))).thenReturn(Optional.of(org));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBOrganization").param("id", "ZPRC").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("ZPRC");
    verify(ucsbOrganizationRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id ZPRC deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_ucsb_organization_and_gets_right_error_message()
      throws Exception {
    // arrange
    when(ucsbOrganizationRepository.findById(eq("NOPE"))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBOrganization").param("id", "NOPE").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbOrganizationRepository, times(1)).findById("NOPE");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id NOPE not found", json.get("message"));
  }
}
