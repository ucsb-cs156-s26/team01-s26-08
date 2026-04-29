package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RecommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

  @Autowired RecommendationRequestRepository recommendationRequestRepository;

  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allRecommendationRequests() {
    Iterable<RecommendationRequest> requests = recommendationRequestRepository.findAll();
    return requests;
  }

  @Operation(summary = "Create a new recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequest(
      @Parameter(description = "Email of the requester") @RequestParam String requesterEmail,
      @Parameter(description = "Email of the professor") @RequestParam String professorEmail,
      @Parameter(description = "Explanation for the request") @RequestParam String explanation,
      @Parameter(description = "Date requested in ISO format")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateRequested,
      @Parameter(description = "Date needed in ISO format")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateNeeded,
      @Parameter(description = "Whether the request is completed") @RequestParam boolean done)
      throws JsonProcessingException {

    RecommendationRequest request = new RecommendationRequest();
    request.setRequesterEmail(requesterEmail);
    request.setProfessorEmail(professorEmail);
    request.setExplanation(explanation);
    request.setDateRequested(dateRequested);
    request.setDateNeeded(dateNeeded);
    request.setDone(done);

    RecommendationRequest savedRequest = recommendationRequestRepository.save(request);

    return savedRequest;
  }

  /**
   * Get a single request by id
   *
   * @param id the id of the request
   * @return a RecommendationRequest
   */
  @Operation(summary = "Get a single request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public RecommendationRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    RecommendationRequest recommendationRequest =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    return recommendationRequest;
  }

  /**
   * Update a single recommendationRequest
   *
   * @param id id of the recommendationRequest to update
   * @param incoming the new recommendationRequest
   * @return the updated recommendationRequest object
   */
  @Operation(summary = "Update a single recommendationRequest")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public RecommendationRequest updateRecommendationRequest(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid RecommendationRequest incoming) {

    RecommendationRequest recommendationRequest =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    recommendationRequest.setRequesterEmail(incoming.getRequesterEmail());
    recommendationRequest.setProfessorEmail(incoming.getProfessorEmail());
    recommendationRequest.setExplanation(incoming.getExplanation());
    recommendationRequest.setDateRequested(incoming.getDateRequested());
    recommendationRequest.setDateNeeded(incoming.getDateNeeded());
    recommendationRequest.setDone(incoming.getDone());

    recommendationRequestRepository.save(recommendationRequest);

    return recommendationRequest;
  }

  /**
   * Delete a RecommendationRequest
   *
   * @param id the id of the recommendationRequest to delete
   * @return a message indicating the recommendationRequest was deleted
   */
  @Operation(summary = "Delete a RecommendationRequest")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteRecommendationRequest(@Parameter(name = "id") @RequestParam Long id) {
    RecommendationRequest recommendationRequest =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    recommendationRequestRepository.delete(recommendationRequest);
    return genericMessage("RecommendationRequest with id %s deleted".formatted(id));
  }
}
