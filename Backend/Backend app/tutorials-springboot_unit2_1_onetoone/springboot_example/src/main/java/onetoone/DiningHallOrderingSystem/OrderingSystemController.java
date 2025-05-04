package onetoone.DiningHallOrderingSystem;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Sonia Patil
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/diningOrders")
public class OrderingSystemController {

    @Autowired
    private final DiningOrderService diningOrderService;

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
}
