package bot.RandomChatBot.service;

import bot.RandomChatBot.models.Product;
import bot.RandomChatBot.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product findByName(String name) {
        return productRepository.findProductByName(name).orElse(null);
    }

    public Double totalPrice() {
        return productRepository.findAll()
                .stream()
                .mapToDouble(c -> c.getPrice() * c.getAmount())
                .sum();
    }

    public void save(Product product) {
        productRepository.save(product);
    }
}
