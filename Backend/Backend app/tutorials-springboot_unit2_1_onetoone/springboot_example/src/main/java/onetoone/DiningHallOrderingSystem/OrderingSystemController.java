package onetoone.DiningHallOrderingSystem;

import lombok.RequiredArgsConstructor;
import onetoone.Bookstore.Products;
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
@RequestMapping("/diningOrders")
public class OrderingSystemController {

    @Autowired
    private DiningOrderService diningOrderService;
    @Autowired
    private DiningOrderRepository diningOrderRepository;

    @PostMapping
    public ResponseEntity<DiningOrder> createDiningOrder(@RequestBody DiningOrder diningOrder) {
        DiningOrder savedDiningOrder = diningOrderService.createDiningOrder(diningOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDiningOrder);
    }

    @GetMapping
    public ResponseEntity<List<DiningOrder>> getDiningOrders() {
        List<DiningOrder> diningOrders = diningOrderService.getDiningOrders();
        return ResponseEntity.ok(diningOrders);
    }

    //get all the products in the diningOrders - doesn't work rn items not getting injected
    @GetMapping("/{id}/orderItems")
    public ResponseEntity<List<DiningOrderItem>> getItems(@PathVariable int id) {
        return diningOrderRepository.findById(id)
                .map(diningOrder -> ResponseEntity.ok(diningOrder.getItems()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DiningOrder> deleteDiningOrder(@PathVariable int id) {
        Optional<DiningOrder> diningOrder = diningOrderRepository.findById(id);
        if (diningOrder.isPresent()) {
            diningOrderRepository.delete(diningOrder.get());
        }
        return ResponseEntity.ok().build();
    }
}
