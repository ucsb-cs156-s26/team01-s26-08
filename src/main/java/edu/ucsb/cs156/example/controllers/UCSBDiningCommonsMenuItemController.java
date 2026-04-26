package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
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

/** This is a REST controller for UCSBDiningCommonsMenuItem */
@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/UCSBDiningCommonsMenuItem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * List all UCSBDiningCommonsMenuItems
   *
   * @return an iterable of UCSBDiningCommonsMenuItems
   */
  @Operation(summary = "List all UCSB Dining Commons Menu Items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> menuItems = ucsbDiningCommonsMenuItemRepository.findAll();
    return menuItems;
  }

  /**
   * Create a new UCSBDiningCommonsMenuItem
   *
   * @param diningCommonsCode code for the dining commons
   * @param name name of the menu item
   * @param station station where the item is served
   * @return the saved UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "Create a new UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station) {

    UCSBDiningCommonsMenuItem menuItem = new UCSBDiningCommonsMenuItem();
    menuItem.setDiningCommonsCode(diningCommonsCode);
    menuItem.setName(name);
    menuItem.setStation(station);

    UCSBDiningCommonsMenuItem savedMenuItem = ucsbDiningCommonsMenuItemRepository.save(menuItem);

    return savedMenuItem;
  }

  /**
   * Get a single UCSBDiningCommonsMenuItem by id
   *
   * @param id the id of the menu item
   * @return the UCSBDiningCommonsMenuItem with the given id
   */
  @Operation(summary = "Get a single UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBDiningCommonsMenuItem getById(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem menuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    return menuItem;
  }

  /**
   * Update a single UCSBDiningCommonsMenuItem
   *
   * @param id id of the menu item to update
   * @param incoming the new menu item values
   * @return the updated menu item object
   */
  @Operation(summary = "Update a single UCSB Dining Commons Menu Item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem menuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    menuItem.setDiningCommonsCode(incoming.getDiningCommonsCode());
    menuItem.setName(incoming.getName());
    menuItem.setStation(incoming.getStation());

    ucsbDiningCommonsMenuItemRepository.save(menuItem);

    return menuItem;
  }
}
