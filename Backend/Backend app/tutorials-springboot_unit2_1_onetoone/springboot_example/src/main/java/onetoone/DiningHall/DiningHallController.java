package onetoone.DiningHall;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller class for managing Dining Halls and their Menu Items.
 * Provides endpoints for CRUD operations and searching.
 *
 * @author Sonia Patil
 */
@RestController
@Tag(name = "DiningHall Management API")
@RequestMapping("/dininghall")
public class DiningHallController {

    @Autowired
    private DiningHallRepository diningHallRepository;

    @Autowired
    private MenuItemsRepository menuItemsRepository;

    /**
     * Get all dining halls.
     *
     * @return a list of all dining halls
     */
    @Operation(summary = "Get all dining halls", description = "Returns a list of all dining hall entities in the DiningHall repository.")
    @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiningHall.class)))
    @GetMapping
    public List<DiningHall> getAllDiningHalls() {
        return diningHallRepository.findAll();
    }

    /**
     * Get a dining hall by its ID.
     *
     * @param id the dining hall ID
     * @return the dining hall entity, or 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a dining hall by ID", description = "Returns the dining hall entity based on the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved Dining Hall",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiningHall.class))),
            @ApiResponse(responseCode = "404", description = "Dining Hall not found")
    })
    public ResponseEntity<DiningHall> getDiningHallById(@PathVariable int id) {
        Optional<DiningHall> diningHall = diningHallRepository.findById(id);
        return diningHall.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new dining hall.
     *
     * @param diningHall the new dining hall entity
     * @return the created dining hall entity
     */
    @PostMapping("/new")
    @Operation(summary = "Create a new dining hall", description = "Creates and returns a new dining hall entity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created a Dining Hall",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiningHall.class))),
    })
    public ResponseEntity<DiningHall> createDiningHall(@RequestBody DiningHall diningHall) {
        DiningHall savedDiningHall = diningHallRepository.save(diningHall);
        return new ResponseEntity<>(savedDiningHall, HttpStatus.CREATED);
    }

    /**
     * Update an existing dining hall.
     *
     * @param id the ID of the dining hall to update
     * @param diningHallDetails the updated dining hall details
     * @return the updated dining hall entity, or 404 if not found
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a dining hall", description = "Updates the details of an existing dining hall by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated Dining Hall",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiningHall.class))),
            @ApiResponse(responseCode = "404", description = "Dining Hall not found")
    })
    public ResponseEntity<DiningHall> updateDiningHall(@PathVariable int id, @RequestBody DiningHall diningHallDetails) {
        return diningHallRepository.findById(id)
                .map(existingDiningHall -> {
                    existingDiningHall.setName(diningHallDetails.getName());
                    existingDiningHall.setLocation(diningHallDetails.getLocation());
                    DiningHall updatedDiningHall = diningHallRepository.save(existingDiningHall);
                    return ResponseEntity.ok(updatedDiningHall);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a dining hall by ID.
     *
     * @param id the ID of the dining hall to delete
     * @return 204 No Content if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a dining hall", description = "Deletes a dining hall by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted Dining Hall",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiningHall.class))),
            @ApiResponse(responseCode = "404", description = "Dining Hall not found")
    })
    public ResponseEntity<Void> deleteDiningHall(@PathVariable int id) {
        return diningHallRepository.findById(id)
                .map(diningHall -> {
                    diningHallRepository.delete(diningHall);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Search dining halls by name.
     *
     * @param name the name to search for
     * @return a list of matching dining halls
     */
    @GetMapping("/search/name")
    @Operation(summary = "Search dining halls by name", description = "Returns a list of dining halls whose names contain the specified text.")
    public List<DiningHall> searchDiningHallsByName(@RequestParam String name) {
        return diningHallRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Search dining halls by location.
     *
     * @param location the location to search for
     * @return a list of matching dining halls
     */
    @GetMapping("/search/location")
    @Operation(summary = "Search dining halls by location", description = "Returns a list of dining halls whose location contains the specified text.")
    public List<DiningHall> searchDiningHallsByLocation(@RequestParam String location) {
        return diningHallRepository.findByLocationContainingIgnoreCase(location);
    }

    /**
     * Search dining halls by menu item name.
     *
     * @param itemName the menu item name to search for
     * @return a list of matching dining halls
     */
    @GetMapping("/search/menuitem")
    @Operation(summary = "Search dining halls by menu item", description = "Returns a list of dining halls that have menu items containing the specified name.")
    public List<DiningHall> searchDiningHallsByMenuItem(@RequestParam String itemName) {
        return diningHallRepository.findByMenuItemsNameContainingIgnoreCase(itemName);
    }

    /**
     * Add a menu item to a dining hall.
     *
     * @param id the ID of the dining hall
     * @param menuItem the menu item to add
     * @return the created menu item entity
     */
    @PostMapping("/{id}/menuitems")
    @Operation(summary = "Add a menu item to a dining hall", description = "Adds a new menu item to a specific dining hall.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully added menu item",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiningHall.class))),
            @ApiResponse(responseCode = "404", description = "Dining Hall not found")
    })
    public ResponseEntity<MenuItems> addMenuItem(@PathVariable int id, @RequestBody MenuItems menuItem) {
        return diningHallRepository.findById(id)
                .map(diningHall -> {
                    menuItem.setDiningHall(diningHall);
                    diningHall.getMenuItems().add(menuItem);
                    diningHallRepository.save(diningHall);
                    return new ResponseEntity<>(menuItem, HttpStatus.CREATED);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a menu item by ID.
     *
     * @param id the ID of the menu item to delete
     * @return 204 No Content if deleted, 404 if not found
     */
    @DeleteMapping("/menuitems/{id}")
    @Operation(summary = "Delete a menu item", description = "Deletes a menu item by its ID and updates the associated dining hall.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted Menu Item",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiningHall.class))),
            @ApiResponse(responseCode = "404", description = "Menu Item not found")
    })
    public ResponseEntity<Void> deleteMenuItem(@PathVariable int id) {
        return menuItemsRepository.findById(id)
                .map(menuItem -> {
                    DiningHall diningHall = menuItem.getDiningHall();
                    if (diningHall != null) {
                        diningHall.getMenuItems().remove(menuItem);
                        diningHallRepository.save(diningHall);
                    }
                    menuItemsRepository.delete(menuItem);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get all menu items for a dining hall.
     *
     * @param id the ID of the dining hall
     * @return a list of menu items associated with the dining hall
     */
    @GetMapping("/{id}/menuitems")
    @Operation(summary = "Get menu items for a dining hall", description = "Returns all menu items associated with the specified dining hall ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved Menu Items",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiningHall.class))),
            @ApiResponse(responseCode = "404", description = "Dining Hall not found")
    })
    public ResponseEntity<List<MenuItems>> getMenuItems(@PathVariable int id) {
        return diningHallRepository.findById(id)
                .map(diningHall -> ResponseEntity.ok(diningHall.getMenuItems()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
