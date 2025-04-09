package onetoone.Bookstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Sonia Patil
 */
@RestController
@RequestMapping("/bookstore")
public class BookstoreController {

    @Autowired
    private BookstoreRepository bookstoreRepository;

    //get all the bookstores (should only be one this is for expansion purposes)
    @GetMapping
    public List<Bookstore> getAllBookstores() {return bookstoreRepository.findAll();}

    //get bookstore by id (again should only be one this is for if a new bookstore opened for some reason)
    @GetMapping("/{id}")
    public ResponseEntity<Bookstore> getDiningHallById(@PathVariable int id) {
        Optional<Bookstore> bookstore = bookstoreRepository.findById(id);
        return bookstore.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //update the bookstore - check on how this works
    @PutMapping("/{id}")
    public ResponseEntity<Bookstore> updateBookstore(@PathVariable int id, @RequestBody Bookstore bookstore) {
        return bookstoreRepository.findById(id)
                .map(existingBookstore -> {
                    existingBookstore.setName(bookstore.getName());
                    existingBookstore.setLocation(bookstore.getLocation());
                    Bookstore updatedBookstore = bookstoreRepository.save(existingBookstore);
                    return ResponseEntity.ok(updatedBookstore);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //delete bookstore (shouldn't happen but the option is there)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookstore(@PathVariable int id) {
        return bookstoreRepository.findById(id)
                .map(bookstore -> {
                    bookstoreRepository.delete(bookstore);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // search dining halls by products - check this later
    @GetMapping("/search/product")
    public List<Bookstore> searchBookstoreByProduct(@RequestParam String productName) {
        return null;
    }

    //add a product to the bookstore
    @PostMapping("/{id}/item")
    public ResponseEntity<Products> addProductToBookstore(@PathVariable int id, @RequestBody Products product){
        return bookstoreRepository.findById(id)
                .map(bookstore -> {
                    product.setBookstore(bookstore);
                    bookstore.getProducts().add(product);
                    bookstoreRepository.save(bookstore);
                    return new ResponseEntity<>(product, HttpStatus.CREATED);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //get all the products in the bookstore
    @GetMapping("{id}/products")
    public ResponseEntity<List<Products>> getProducts(@PathVariable int id) {
        return bookstoreRepository.findById(id)
                .map(bookstore -> ResponseEntity.ok(bookstore.getProducts()))
                .orElseGet(() -> ResponseEntity.notFound().build());
                }
}
