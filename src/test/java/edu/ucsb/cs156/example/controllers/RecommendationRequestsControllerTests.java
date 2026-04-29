package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestsControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  @Autowired ObjectMapper mapper;

  // Authorization tests for /api/recommendationrequests/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequests/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().is(200)); // logged
  }

  // Authorization tests for /api/recommendationrequests/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("professorEmail", "professor@ucsb.edu")
                .param("explanation", "Please write me a recommendation.")
                .param("dateRequested", "2026-04-28T00:00:00")
                .param("dateNeeded", "2026-05-10T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequests/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("professorEmail", "professor@ucsb.edu")
                .param("explanation", "Please write me a recommendation.")
                .param("dateRequested", "2026-04-28T00:00:00")
                .param("dateNeeded", "2026-05-10T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsbdates() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-01-03T00:00:00");

    RecommendationRequest recommendationRequest1 =
        RecommendationRequest.builder()
            .requesterEmail("firstDayOfClasses")
            .professorEmail("20222")
            .explanation("20222")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(true)
            .build();

    ArrayList<RecommendationRequest> expectedRecommendationRequests = new ArrayList<>();
    expectedRecommendationRequests.add(recommendationRequest1);

    when(recommendationRequestRepository.findAll()).thenReturn(expectedRecommendationRequests);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequests/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRecommendationRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_ucsbdate() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-01-03T00:00:00");

    RecommendationRequest recommendationRequest1 =
        RecommendationRequest.builder()
            .requesterEmail("firstDayOfClasses")
            .professorEmail("20222")
            .explanation("20222")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(true)
            .build();

    when(recommendationRequestRepository.save(eq(recommendationRequest1)))
        .thenReturn(recommendationRequest1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequests/post?requesterEmail=firstDayOfClasses"
                        + "&professorEmail=20222"
                        + "&explanation=20222"
                        + "&dateRequested=2022-01-03T00:00:00"
                        + "&dateNeeded=2022-01-03T00:00:00"
                        + "&done=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).save(eq(recommendationRequest1));
    String expectedJson = mapper.writeValueAsString(recommendationRequest1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
