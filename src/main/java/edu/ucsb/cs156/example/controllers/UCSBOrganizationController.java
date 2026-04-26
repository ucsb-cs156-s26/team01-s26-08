package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBOrganization */
@Tag(name = "UCSBOrganization")
@RequestMapping("/api/UCSBOrganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {

  @Autowired UCSBOrganizationRepository ucsbOrganizationRepository;

  /**
   * List all UCSBOrganizations
   *
   * @return an iterable of UCSBOrganizations
   */
  @Operation(summary = "List all ucsb organizations")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBOrganization> allUCSBOrganizations() {
    return ucsbOrganizationRepository.findAll();
  }

  /**
   * Update a single UCSBOrganization. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param id organization code (primary key)
   * @param incoming the new organization contents (orgCode is ignored)
   * @return the updated organization object
   */
  @Operation(summary = "Update a single ucsb organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBOrganization updateUCSBOrganization(
      @Parameter(name = "id") @RequestParam(name = "id") String id,
      @RequestBody @Valid UCSBOrganization incoming) {

    UCSBOrganization org =
        ucsbOrganizationRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, id));

    org.setOrgTranslationShort(incoming.getOrgTranslationShort());
    org.setOrgTranslation(incoming.getOrgTranslation());
    org.setInactive(incoming.getInactive());

    ucsbOrganizationRepository.save(org);

    return org;
  }

  /**
   * Create a new UCSBOrganization
   *
   * @param orgCode organization code (primary key)
   * @param orgTranslationShort short translation for organization name
   * @param orgTranslation full translation for organization name
   * @param inactive whether the organization is inactive
   * @return the saved UCSBOrganization
   */
  @Operation(summary = "Create a new ucsb organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBOrganization postUCSBOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @Parameter(name = "orgTranslationShort") @RequestParam String orgTranslationShort,
      @Parameter(name = "orgTranslation") @RequestParam String orgTranslation,
      @Parameter(name = "inactive") @RequestParam boolean inactive) {

    UCSBOrganization org = new UCSBOrganization();
    org.setOrgCode(orgCode);
    org.setOrgTranslationShort(orgTranslationShort);
    org.setOrgTranslation(orgTranslation);
    org.setInactive(inactive);

    return ucsbOrganizationRepository.save(org);
  }
}
