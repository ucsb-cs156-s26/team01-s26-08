package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
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

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

  @MockitoBean HelpRequestRepository helpRequestRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/HelpRequest/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc.perform(get("/api/HelpRequest").param("id", "7")).andExpect(status().is(403));
  }

  // Authorization tests for /api/HelpRequest/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/HelpRequest/post")
                .param("requesterEmail", "student1@ucsb.edu")
                .param("teamId", "team01")
                .param("tableOrBreakoutRoom", "4")
                .param("requestTime", "2026-04-25T16:00:00")
                .param("explanation", "Need help with Dokku")
                .param("solved", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/HelpRequest/post")
                .param("requesterEmail", "student1@ucsb.edu")
                .param("teamId", "team01")
                .param("tableOrBreakoutRoom", "4")
                .param("requestTime", "2026-04-25T16:00:00")
                .param("explanation", "Need help with Dokku")
                .param("solved", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime requestTime = LocalDateTime.parse("2026-04-25T16:00:00");

    HelpRequest helpRequest =
        HelpRequest.builder()
            .requesterEmail("student1@ucsb.edu")
            .teamId("team01")
            .tableOrBreakoutRoom("4")
            .requestTime(requestTime)
            .explanation("Need help with Dokku")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(helpRequest));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/HelpRequest").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(helpRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange
    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/HelpRequest").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_help_requests() throws Exception {

    // arrange
    LocalDateTime requestTime1 = LocalDateTime.parse("2026-04-25T16:00:00");
    LocalDateTime requestTime2 = LocalDateTime.parse("2026-04-25T16:15:00");

    HelpRequest helpRequest1 =
        HelpRequest.builder()
            .requesterEmail("student1@ucsb.edu")
            .teamId("team01")
            .tableOrBreakoutRoom("4")
            .requestTime(requestTime1)
            .explanation("Need help with Dokku")
            .solved(false)
            .build();

    HelpRequest helpRequest2 =
        HelpRequest.builder()
            .requesterEmail("student2@ucsb.edu")
            .teamId("team02")
            .tableOrBreakoutRoom("Breakout room 2")
            .requestTime(requestTime2)
            .explanation("Need help with tests")
            .solved(true)
            .build();

    ArrayList<HelpRequest> expectedHelpRequests = new ArrayList<>();
    expectedHelpRequests.addAll(Arrays.asList(helpRequest1, helpRequest2));

    when(helpRequestRepository.findAll()).thenReturn(expectedHelpRequests);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().isOk()).andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedHelpRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_help_request() throws Exception {
    // arrange
    LocalDateTime requestTime = LocalDateTime.parse("2026-04-25T16:00:00");

    HelpRequest helpRequest =
        HelpRequest.builder()
            .requesterEmail("student1@ucsb.edu")
            .teamId("team01")
            .tableOrBreakoutRoom("4")
            .requestTime(requestTime)
            .explanation("Need help with Dokku")
            .solved(true)
            .build();

    when(helpRequestRepository.save(eq(helpRequest))).thenReturn(helpRequest);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/HelpRequest/post")
                    .param("requesterEmail", "student1@ucsb.edu")
                    .param("teamId", "team01")
                    .param("tableOrBreakoutRoom", "4")
                    .param("requestTime", "2026-04-25T16:00:00")
                    .param("explanation", "Need help with Dokku")
                    .param("solved", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).save(helpRequest);
    String expectedJson = mapper.writeValueAsString(helpRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
