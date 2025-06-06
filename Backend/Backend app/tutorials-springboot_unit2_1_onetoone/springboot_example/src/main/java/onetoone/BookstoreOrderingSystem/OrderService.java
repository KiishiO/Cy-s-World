package onetoone.BookstoreOrderingSystem;


import lombok.RequiredArgsConstructor;
import onetoone.Bookstore.Products;
import onetoone.Bookstore.ProductsRepository;
import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ProductsRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(Order order) {
        int personId = order.getPerson().getId();
        Person person = personRepository.findById(personId);
                //.orElseThrow(() -> new RuntimeException("Person not found"));

        order.setPerson(person);
        order.setOrderDate(LocalDateTime.now());

        for (OrderItem item : order.getItems()) {
            int productId = item.getProduct().getId();
            Products product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            item.setProduct(product);
            item.setOrder(order);
        }

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}

